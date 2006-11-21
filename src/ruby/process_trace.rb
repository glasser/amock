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

  os.doc.root.each_element do |e|
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
    puts "import org.jmock.cglib.MockObjectTestCase;"
    puts "import org.jmock.Mock;"
    imports.each_value do |full_name|
      puts "import #{full_name};"
    end
    puts
    puts "public class GeneratedTests extends MockObjectTestCase {"
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
  attr_reader :generator, :trace_id, :lines, :other_objects
  attr_accessor :handler

  def initialize(trace_id, generator)
    @trace_id = trace_id
    @generator = generator
    @lines = []
    @other_objects = {}
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
  
  def build_method_call(name, args, retval)
    line = ""
    line += "assertEquals(#{javafy_item(retval)}, " if retval
    line += "testedObject.#{name}("
    line += args.elements.collect {|a| javafy_item(a)}.join(', ')
    line += ")"
    line += ")" if retval
    line += ";"
    return line
  end

  def javafy_item(item)
    case item.name
    when "primitive"
      # XXX: Special case character
      return item.attributes["value"]
    when "null"
      return "null"
    when "string"
      return item.text # XXX escaping!
    when "object"
      return javafy_reference(item)
    end
  end

  def javafy_reference(item)
    other_class = generator.get_classname(item.attributes['class'])
    other_id = item.attributes['id']
    
    # TODO: better names
    mock_name = "mock#{other_id}"
    proxy_name = "proxy#{other_id}"
    unless other_objects[mock_name]
      # XXX need to make a mock
      lines << "Mock #{mock_name} = mock(#{other_class}.class);"
      lines << "#{other_class} #{proxy_name} = (#{other_class}) #{mock_name}.proxy();"
      other_objects[mock_name] = true
    end
    return proxy_name
  end
end

def action_is_constructor(action, classname)
  if action.name == 'postCall' and action.attributes['name'] == '<init>' and
      internal_to_external_classname(action.attributes['owner']) == classname
    return action.elements['receiver/object'].attributes['id']
  else
    return false
  end
end

def internal_to_external_classname(classname)
  classname.gsub '/', '.'
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
    if action.name == 'preCall' and 
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
    if action.name == 'postCall' and action.attributes['call'] == @callid
      retval = action.elements['void'] ? nil : action.elements['return'].elements[1]
      sm << sm.build_method_call(action.attributes['name'],
                                 action.elements['args'], retval)

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

main
