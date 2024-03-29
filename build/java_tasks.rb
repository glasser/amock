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

def short_name(cls)
  cls.split('.').last
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

def set_default_classpath(cp)
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
    
    command = %w{javac -Xlint:unchecked}
    command.push "-cp", classpath.join(':')
    command.push "-d", destination if destination
    command += sources
    
    sh *command
  end
end

# Shortcut to the Javac task creation. Makes it handy.
def javac(args, &block)
  JavacTask.define_task(args, &block)
end

class JarTask < JavaTask
  attr_accessor :source_dir, :destination, :manifest

  def execute
    super

    fail "jar task #{name} must define source_dir" unless source_dir
    fail "jar task #{name} must define destination" unless destination

    directive = '-cf'
    directive += 'm' if manifest
    
    command = ['fastjar', directive]
    command << destination
    command << manifest if manifest
    command.push "-C", source_dir, "."

    sh *command
  end
end

# Shortcut to the Jar task creation. Makes it handy.
def jar(args, &block)
  JarTask.define_task(args, &block)
end

class RunJavaTask < JavaTask
  attr_accessor :classname, :premain_agent, :premain_options

  def args
    @args ||= []
  end

  def env
    @env ||= {}
  end

  def vm_args
    @vm_args || []
  end

  def execute
    super

    fail "java task #{name} must define classname" unless classname
    
    command = %w{time java}
    command.push "-cp", classpath.join(':')
    command << '-ea' # assertions

    if premain_agent
      premain_command = "-javaagent:" + premain_agent
      premain_command += "=#{premain_options}" if premain_options
      command << premain_command
    end

    command += vm_args

    command << classname
    command += args

    puts "JAVA: #{short_name(classname)}"
    
    with_environment env do
      sh *command
    end
  end
end

# Shortcut to the Java task creation. Makes it handy.
def java(args, &block)
  RunJavaTask.define_task(args, &block)
end

class JunitTask < RunJavaTask
  attr_accessor :suite

  def classname
    'junit.textui.TestRunner'
  end

  def args
    [suite]
  end
end

def junit(args, &block)
  JunitTask.define_task(args, &block)
end

def gunzip(unzippable)
  file unzippable => unzippable+".gz" do |t|
    sh "gunzip -c #{t.prerequisites} > #{t.name}"
  end
end

def with_environment(new_env_vars, &block)
  if new_env_vars.empty?
    yield
    return
  end

  saved_env = {}
  saved_env.replace(ENV)
  
  ENV.update(new_env_vars)

  yield

  ENV.replace(saved_env)
end
