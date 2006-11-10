require 'rexml/document'
require 'optparse'
require 'ostruct'

def main
  opts = OptionParser.new
  os = OpenStruct.new

  opts.on("-t", "--trace-file FILE", "input trace file (mandatory)") do |fn|
    os.doc = REXML::Document.new(File.new(fn))
  end
  opts.on("-c", "--extract-class CLASSNAME",
          "fully qualified java class name to extract (mandatory)") do |cn|
    os.classname = cn
  end
  opts.on("-o", "--output-file OUT", "write JUnit code to this file") do |fn|
    $stdout.reopen(fn, "w")
  end

  leftovers = opts.parse!

  unless os.doc and os.classname and leftovers.empty?
    puts opts.help
    exit
  end

  processor = JUnitGenerator.new(os.classname)

  os.doc.each_element("//action") do |e| # ("//object[@id=#{os.trace_id}]/ancestor-or-self::action") do |e|
    processor.process_action e
  end

  processor.print_file
end

class JUnitGenerator
  attr_reader :classname, :processors, :imports
  
  def initialize(classname)
    @classname = classname
    @processors = []
    @imports = {}
  end
  
  def process_action(action)
    if trace_id = action_is_constructor(action, classname)
      processors << TestProcessor.new(trace_id, self)
    end

    processors.each {|p| p.process_action(action)}
  end

  def print_file
    print_file_header
    processors.each {|p| p.print_method}
    print_file_footer
  end

  def print_file_header
    puts "package edu.mit.csail.pag.amock.subjects.generated;"
    puts
    puts "import junit.framework.TestCase;"
    imports.each_value do |full_name|
      puts "import #{full_name};"
    end
    puts
    puts "public class GeneratedTests extends TestCase {"
  end

  def print_file_footer
    puts "}"
  end

  def get_classname(classname)
    unless m = classname.match(/\.(\w+)$/)
      raise "Weird class name: #{classname}"
    end
    
    shortname = m[1]
    if imports[shortname] and imports[shortname] != classname
      return classname
    else
      imports[shortname] = classname
      return shortname
    end
  end
end

class TestProcessor
  attr_reader :generator, :trace_id, :lines
  attr_accessor :handler

  def initialize(trace_id, generator)
    @trace_id = trace_id
    @generator = generator
    @lines = []
    self.handler = WaitForConstructorToEnd.new
  end

  def process_action(action)
    handler.process_action(action, self)
  end

  def next_state(s)
    self.handler = s
  end
 
  def <<(line)
    lines << line
  end

  def print_method
    print_method_header
    lines.each {|l| puts "        #{l}" }
    print_method_footer
  end

  def print_method_header
    puts "    public void test#{trace_id}() {"
  end

  def print_method_footer
    puts "    }"
    puts
  end

  def build_constructor(classname, args)
    classname = generator.get_classname(classname)
    return "#{classname} testedObject = new #{classname}(" +
      args.elements.collect {|a| javafy_item(a)}.join(', ') +
      ");"
  end
  
  def build_method_call(signature, args, retval)
    line = ""
    line += "assertEquals(#{javafy_item(retval)}, " if retval
    line += "testedObject.#{method_name_from_signature signature}("
    line += args.elements.collect {|a| javafy_item(a)}.join(', ')
    line += ")"
    line += ")" if retval
    line += ";"
    return line
  end
end

def action_is_constructor(action, classname)
  if action.attributes['type'] == 'exit' and 
      action.attributes['signature'] =~ /^#{classname}\.<init>\(/

      return action.elements['receiver/object'].attributes['id']
  else
    return false
  end
end
  
class TestProcessorHandler; end

class WaitForConstructorToEnd < TestProcessorHandler
  def process_action(action, sm)
    if trace_id = action_is_constructor(action, sm.generator.classname) and
        trace_id == sm.trace_id

      sm << sm.build_constructor(sm.generator.classname, action.elements['args'])
      
      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

class MonitorCallsOnObject < TestProcessorHandler
  def process_action(action, sm)
    if action.attributes['type'] == 'enter' and 
        action.elements["receiver[object[@id=#{sm.trace_id}]]"]
      # We'll print out this call when it returns.
      sm.next_state(WaitForMethodToEnd.new(action.attributes['call']))
    end
  end
end

class WaitForMethodToEnd < TestProcessorHandler
  def initialize(callid)
    @callid = callid
  end
  
  def process_action(action, sm)
    if action.attributes['type'] == 'exit' and action.attributes['call'] == @callid
      retval = action.elements['void'] ? nil : action.elements['return'].elements[1]
      sm << sm.build_method_call(action.attributes['signature'], 
                                 action.elements['args'], retval)

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

def javafy_item(item)
  case item.name
  when "primitive"
    return item.attributes["value"]
  when "null"
    return "null"
  when "string"
    return item.text # XXX escaping!
  when "object"
    raise "Reference objects not yet supported"
  end
end

def method_name_from_signature(sig)
  # Assumes that the method is not static.
  m = sig.match /^(?:\w+\.)+(\w+)\(/
  raise "unparsable signature: #{sig}" unless m
  return m[1]
end
  

main