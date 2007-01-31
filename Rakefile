# Rakefile for amock.

require 'build/java_tasks'

CLASSES = "bin/classes"
AMOCK_JAR = "bin/amock.jar"
SUBJECTS_BIN = "subjects/bin"
SUBJECTS_OUT = "subjects/out"

set_default_classpath FileList["lib/java/*.jar"]
default_classpath  <<  SUBJECTS_BIN
default_classpath  <<  CLASSES


# The default task is changing during development to be whatever is
# most relevant to current development.

task :default => [:clean, :check, :bakery]



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

task :tags do |t|
  sh "find src/java -name '*.java' | xargs etags -o src/java/TAGS"
end

directory SUBJECTS_OUT

task :prepare_subjects => [AMOCK_JAR, SUBJECTS_OUT, :build_subjects]

class AmockTestDescription
  attr_accessor :system_test, :identifier, :unit_test, :test_method, :tested_class
end

def amock_test
  a = AmockTestDescription.new()
  yield(a)

  i = a.identifier

  trace_file = "#{SUBJECTS_OUT}/#{i}-trace.xml"
  unit_test_file = "#{SUBJECTS_OUT}/#{a.unit_test}.java"

  java :"#{i}_trace" => :prepare_subjects do |t|
    t.classname = a.system_test
    t.premain_agent = AMOCK_JAR
    t.premain_options = "--tracefile=#{trace_file}"
  end
  
  java :"#{i}_process" => :"#{i}_trace" do |t|
    t.classname = amock_class('processor.Processor')
    t.args << trace_file
    t.args << unit_test_file
    t.args << a.unit_test
    t.args << a.test_method
    t.args << a.tested_class
  end

  javac :"#{i}_compile" => :"#{i}_process" do |t|
    t.sources = [unit_test_file]
    t.destination = SUBJECTS_BIN
  end
  
  junit :"#{i}_try" => :"#{i}_compile" do |t|
    t.suite = amock_class("subjects.generated.#{a.unit_test}")
  end

  task i.to_sym => :"#{i}_try"
end

amock_test do |a|
  a.system_test = amock_class('subjects.bakery.Bakery')
  a.identifier = :bakery
  a.unit_test = 'AutoCookieMonsterTest'
  a.test_method = "cookieEating"
  a.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster"
end

amock_test do |a|
  a.system_test = amock_class('subjects.bakery.Bakery')
  a.identifier = :named_bakery
  a.unit_test = 'AutoNamedCookieMonsterTest'
  a.test_method = "cookieEating"
  a.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/NamedCookieMonster"
end

junit :check_unit => [:build, :build_subjects] do |t|
  t.suite = amock_class('tests.UnitTestSuite')
end

java :generate_cookie_eating => [:build, :build_subjects, SUBJECTS_OUT] do |t|
  t.classname = amock_class('tests.TestMethodGeneratorTests')
  t.args << "#{SUBJECTS_OUT}/CookieMonsterTest.java"
end

javac :compile_cookie_eating => [:generate_cookie_eating] do |t|
  t.sources = ["#{SUBJECTS_OUT}/CookieMonsterTest.java"]
  t.destination = SUBJECTS_BIN
end

junit :run_cookie_eating => [:compile_cookie_eating] do |t|
  t.suite = amock_class('subjects.generated.CookieMonsterTest')
end

task :check_system => [:run_cookie_eating]

task :check => [:check_unit, :check_system]
