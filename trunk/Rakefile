# Rakefile for amock.

require 'build/java_tasks'

SUBJECTS_BIN = "subjects/bin"
CLASSES = "bin/classes"
AMOCK_JAR = "bin/amock.jar"

set_default_classpath FileList["lib/*.jar"]
default_classpath  <<  SUBJECTS_BIN


def amock_class(name)
  'edu.mit.csail.pag.amock.' + name
end

directory SUBJECTS_BIN

javac :build_subjects => [SUBJECTS_BIN] do |t|
  t.sources = FileList["subjects/src/**/*.java"]
  t.destination = SUBJECTS_BIN
end

directory CLASSES

javac :build => [CLASSES] do |t|
  t.sources = FileList["src/**/*.java"]
  t.destination = CLASSES
end

jar AMOCK_JAR => [:build] do |t|
  t.source_dir = CLASSES
  t.destination = AMOCK_JAR
  t.manifest = "src/manifest.txt"
end

task :jar => [AMOCK_JAR]

task :clean do
  [SUBJECTS_BIN, "bin"].each do |fn|
    rm_r fn rescue nil
  end
end

java :pibst => [:build_subjects] do |t|
  t.classname = amock_class('subjects.PositiveIntBoxSystemTest')
end

java :ptrace => [:build_subjects] do |t|
  t.classname = amock_class('subjects.PositiveIntBoxSystemTest')
  t.premain_agent = AMOCK_JAR
end

task :default => [:jar]
