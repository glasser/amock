# Rakefile for amock.

require 'build/java_tasks'

CLASSES = "bin/classes"
AMOCK_JAR = "bin/amock.jar"
SUBJECTS_BIN = "subjects/bin"
SUBJECTS_OUT = "subjects/out"

set_default_classpath FileList["lib/java/*.jar"]
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
  t.sources = FileList["src/java/**/*.java"]
  t.destination = CLASSES
end

jar AMOCK_JAR => [:build] do |t|
  t.source_dir = CLASSES
  t.destination = AMOCK_JAR
  t.manifest = "src/manifest.txt"
end

task :jar => [AMOCK_JAR]

task :clean do
  [SUBJECTS_BIN, SUBJECTS_OUT, "bin"].each do |fn|
    rm_r fn rescue nil
  end
end

java :pibst => [:build_subjects] do |t|
  t.classname = amock_class('subjects.PositiveIntBoxSystemTest')
end

directory SUBJECTS_OUT

java :ptrace => [AMOCK_JAR, SUBJECTS_OUT, :build_subjects] do |t|
  t.classname = amock_class('subjects.PositiveIntBoxSystemTest')
  t.premain_agent = AMOCK_JAR
  t.premain_options="--tracefile=#{SUBJECTS_OUT}/pibst-trace.xml"
end

task :validate_trace => [:ptrace] do |t|
  sh *%W{tools/jing -c tools/trace-schema.rnc #{SUBJECTS_OUT}/pibst-trace.xml}
end

task :process => [:validate_trace] do |t|
  sh *%W{ruby src/ruby/process_trace.rb
         --trace-file #{SUBJECTS_OUT}/pibst-trace.xml
         --extract-class edu.mit.csail.pag.amock.subjects.PositiveIntBox
         --output-file #{SUBJECTS_OUT}/GeneratedTests.java}
end

javac :compile_processed => [:process] do |t|
  t.sources = ["#{SUBJECTS_OUT}/GeneratedTests.java"]
  t.destination = SUBJECTS_BIN
end

java :run_processed => [:compile_processed] do |t|
  t.classname = 'junit.textui.TestRunner'
  t.args << amock_class('subjects.generated.GeneratedTests')
end

task :default => [:run_processed]
