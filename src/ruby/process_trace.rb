#!/usr/bin/env ruby
require 'rexml/document'
require 'optparse'
require 'ostruct'

require 'junit-gen'
require 'trace-handlers'

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

main
