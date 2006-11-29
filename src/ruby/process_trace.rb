#!/usr/bin/env ruby
require 'rexml/document'
require 'optparse'
require 'ostruct'

require 'junit-gen'

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
