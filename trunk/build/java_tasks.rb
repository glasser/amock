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

class JavaTask < Task
  attr_accessor :classpath

  def self.default_classpath
    @@default_classpath ||= []
  end

  def self.default_classpath=(new)
    @@default_classpath = new
  end

  def initialize(*args)
    super
    self.classpath = [ *default_classpath ] unless self.classpath
  end
end

def default_classpath=(cp)
  JavaTask.default_classpath = cp
end

def default_classpath
  JavaTask.default_classpath
end

class JavacTask < JavaTask
  attr_accessor :sources, :destination

  def execute
    super

    fail "javac task #{name} must define sources" unless sources
    
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

class RunJavaTask < JavaTask
  attr_accessor :classname, :premain_agent

  def args
    @args ||= []
  end

  def execute
    super

    fail "java task #{name} must define classname" unless classname
    
    command = %w{java}
    command.push "-cp", classpath.join(':')
    command << '-ea' # assertions
    command << ("-javaagent:" + premain_agent) if premain_agent
    command << classname
    command.push *args
    
    sh *command
  end
end

# Shortcut to the Java task creation. Makes it handy.
def java(args, &block)
  RunJavaTask.define_task(args, &block)
end
