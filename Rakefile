# Rakefile for amock.

require 'build/java_tasks'

SUBJECTS_BIN = "subjects/bin"

CLASSPATH = FileList["lib/*.jar"]
CLASSPATH << SUBJECTS_BIN

def amock_class(name)
  'edu.mit.csail.pag.amock.' + name
end

directory SUBJECTS_BIN

javac :build_subjects => [SUBJECTS_BIN] do |t|
  t.sources = FileList["subjects/src/**/*.java"]
  t.classpath = CLASSPATH
  t.destination = SUBJECTS_BIN
end

task :clean do
  [SUBJECTS_BIN].each do |fn|
    rm_r fn rescue nil
  end
end

java :pibst => [:build_subjects] do |t|
  t.classname = amock_class('subjects.PositiveIntBoxSystemTest')
  t.classpath = CLASSPATH
end

task :default => [:build_subjects]
