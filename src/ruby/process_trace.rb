require 'rexml/document'
require 'optparse'
require 'ostruct'

ARGV.push "--trace-file", "subjects/out/pibst-trace.xml", "--extract-class", "edu.mit.csail.pag.amock.subjects.PositiveIntBox", "--extract-trace-id", "5"

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

  os.doc.each_element("//object[@id=#{os.trace_id}]/ancestor-or-self::action") do |e|
    processor.process_action e
  end

  processor = WaitForConstructorToEnd.new
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
        puts "construct an object with args:"
      puts action.elements['args']
      
      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

class MonitorCallsOnObject < StateMachineHandler
  def process_action(action, sm)
    if action.attributes['type'] == 'enter' and action.elements["receiver[object[@id=#{sm.trace_id}]]"]
      puts "call a method #{action.attributes['signature']} on it (id #{action.attributes['call']}); args"
      puts action.elements['args']

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
      puts "we return:"
      if action.elements['void']
        puts "(void)"
      else
        puts "retval:"
        puts action.elements['return']
      end

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end



main
