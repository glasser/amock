# Rakefile for amock.

require 'build/java_tasks'

CLASSES = "bin/classes"
AMOCK_JAR = "bin/amock.jar"
SMOCK_JAR = "bin/smock.jar"
SUBJECTS_BIN = "subjects/bin"
SUBJECTS_OUT = "subjects/out"

set_default_classpath FileList["lib/java/*.jar"]
default_classpath  <<  SUBJECTS_BIN
default_classpath  <<  CLASSES


# The default task is changing during development to be whatever is
# most relevant to current development.

task :default => [:clean, :check, :amockify_contrived, :amockify_real_pass]

task :amockify_contrived => [:bakery, :fields, :rect, :rect_tweak, 
                             :hierarchy, :staticfield, :static, :capture, :joke]

task :amockify_real_pass => [:jmodeller_try]

task :amockify_real_xfail => [:svnkit]

def amock_class(name)
  'edu.mit.csail.pag.amock.' + name
end

def smock_class(name)
  'edu.mit.csail.pag.smock.' + name
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

jar SMOCK_JAR => [:build] do |t|
  t.source_dir = CLASSES
  t.destination = SMOCK_JAR
  t.manifest = "src/smock-manifest.txt"
end

task :jar => [AMOCK_JAR, SMOCK_JAR]

task :clean do
  [SUBJECTS_BIN, SUBJECTS_OUT, "bin"].each do |fn|
    rm_r fn rescue nil
  end
end

task :tags do |t|
  sh "find src/java -name '*.java' | xargs etags -o src/java/TAGS"
end

directory SUBJECTS_OUT

task :prepare_subjects => [AMOCK_JAR, SUBJECTS_OUT, :build_subjects]

junit :check_unit => [:build, :build_subjects] do |t|
  t.suite = amock_class('tests.UnitTestSuite')
end

junit :check_smock => [:build, :build_subjects, SMOCK_JAR] do |t|
  t.suite = smock_class('tests.SmockTestSuite')
  t.premain_agent = SMOCK_JAR
end

require 'build/system_tests'

java :generate_cookie_eating => [:build, :build_subjects, SUBJECTS_OUT] do |t|
  t.classname = amock_class('tests.TestMethodGeneratorTests')
  t.args << "#{SUBJECTS_OUT}/CookieMonsterTest.java"
end

javac :compile_cookie_eating => [:generate_cookie_eating] do |t|
  t.sources = ["#{SUBJECTS_OUT}/CookieMonsterTest.java"]
  t.destination = SUBJECTS_BIN
end

junit :run_cookie_eating => [:compile_cookie_eating] do |t|
  t.suite = 'CookieMonsterTest'
end

task :check_system => [:run_cookie_eating]

task :check => [:check_unit, :check_smock, :check_system]


# You can set env variables at the command line: 
#  $ rake rpci CLASS=edu/mit/csail/pag/amock/subjects/fields/Book
java :rpci => [:build, :build_subjects] do |t|
  t.classname = amock_class('hooks.RecordPrimaryClassInfo')
  t.args << ENV["CLASS"]
end

# You can set env variables at the command line: 
#  $ rake ipci CLASS=CH/ifa/draw/framework/FigureEnumeration
java :ipci => [:build, :build_subjects] do |t|
  t.classname = amock_class('hooks.IterationPrimaryClassInfo')
  t.args << ENV["CLASS"]
end

# You can set env variables at the command line: 
#  $ rake sfpci CLASS=CH/ifa/draw/framework/FigureEnumeration
java :sfpci => [:build, :build_subjects] do |t|
  t.classname = amock_class('hooks.StaticFieldPrimaryClassInfo')
  t.args << ENV["CLASS"]
end
