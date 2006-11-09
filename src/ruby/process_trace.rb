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
  opts.on("-i", "--extract-trace-id ID", OptionParser::DecimalInteger,
          "internal trace id of object to extract (mandatory)") do |i|
    os.trace_id = i
  end

  leftovers = opts.parse!

  unless os.doc and os.classname and os.trace_id and leftovers.empty?
    puts opts.help
    exit
  end

  processor = StateMachine.new(os.classname, os.trace_id)

  print_file_header

  os.doc.each_element("//object[@id=#{os.trace_id}]/ancestor-or-self::action") do |e|
    processor.process_action e
  end

  print_method_footer # XXX should allow multiple methods
  print_file_footer
end

class StateMachine
  attr_reader :classname, :trace_id
  attr_accessor :handler

  def initialize(classname_in, id_in)
    @classname = classname_in
    @trace_id = id_in
    self.handler = WaitForConstructorToEnd.new
  end

  def process_action(action)
    handler.process_action(action, self)
  end

  def next_state(s)
    self.handler = s
  end
end

class StateMachineHandler; end

class WaitForConstructorToEnd < StateMachineHandler
  def process_action(action, sm)
    if action.attributes['type'] == 'exit' and 
        action.attributes['signature'] =~ /^#{sm.classname}\.<init>/

      print_method_header(sm.trace_id)
      print_constructor(sm.classname, action.elements['args'])
      
      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

class MonitorCallsOnObject < StateMachineHandler
  def process_action(action, sm)
    if action.attributes['type'] == 'enter' and action.elements["receiver[object[@id=#{sm.trace_id}]]"]
      # We'll print out this call when it returns.
      sm.next_state(WaitForMethodToEnd.new(action.attributes['call']))
    end
  end
end

class WaitForMethodToEnd < StateMachineHandler
  def initialize(callid)
    @callid = callid
  end
  
  def process_action(action, sm)
    if action.attributes['type'] == 'exit' and action.attributes['call'] == @callid
      retval = action.elements['void'] ? nil : action.elements['return'].elements[1]
      print_method_call(action.attributes['signature'], action.elements['args'], retval)

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

def print_method_header(trace_id)
  puts "    public void test#{trace_id}() {"
end

def print_constructor(classname, args)
  print "        #{classname} testedObject = new #{classname}("
  print args.elements.collect {|a| javafy_item(a)}.join(', ')
  puts ");"
end

def print_method_call(signature, args, retval)
  print "        assertEquals(#{javafy_item(retval)}, " if retval
  print "testedObject.#{signature}("
  print args.elements.collect {|a| javafy_item(a)}.join(', ')
  print ")"
  print ")" if retval
  puts ";"
end

def print_method_footer
  puts "    }"
end

def print_file_header
  puts <<-'END_HEADER'
package edu.mit.csail.pag.amock.subjects.generated;
 
import junit.framework.TestCase;

public class GeneratedTests extends TestCase {
  END_HEADER
end

def print_file_footer
  puts "}"
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
  

main
