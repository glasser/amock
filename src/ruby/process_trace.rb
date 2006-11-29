#!/usr/bin/env ruby
require 'rexml/document'
require 'optparse'
require 'ostruct'

require 'junit-gen'
require 'trace-handlers'
require 'trace-actions'

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

  processor = TestClassGenerator.new(os.classname)

  os.doc.root.each_element do |e|
    processor.process_action TraceAction.new(e)
  end

  processor.print_file
end

def action_is_constructor(action, classname)
  if action.type == 'postCall' and action.method_name == '<init>' and
      action.owner_external == classname
    return action.receiver.object_id
  else
    return false
  end
end

main
