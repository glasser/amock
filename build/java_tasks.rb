#!/usr/bin/env ruby

# Java tasks for Rake.
#
# Inspired by Matthieu Riou's Raven java tasks.
#
# David Glasser, amock project, 2006

require 'rake'

begin
  require 'rake/classic_namespace' # so this can be used on Rake 0.5 and 0.6+
rescue LoadError
end

class JavacTask < Task
  attr_accessor :sources, :destination, :classpath

  def execute
    super

    fail "javac task #{name} must define sources" unless sources
    fail "javac task #{name} must define classpath" unless classpath
    
    command = %w{javac}
    command.push "-cp", classpath.join(':')
    command.push "-d", destination if destination
    command.push *sources
    
    sh *command
  end
end

# Shortcut to the Javac task creation. Makes it handy.
def javac(args, &block)
  JavacTask.define_task(args, &block)
end
