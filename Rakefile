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
  attr_accessor :system_test, :identifier
  attr_reader :unit_tests

  def unit_test
    u = UnitTestDescription.new
    yield(u)
    
    @unit_tests ||= []
    unit_tests << u
  end
end

class UnitTestDescription
  attr_accessor :unit_test, :test_method, :tested_class, :identifier
end

def amock_test
  a = AmockTestDescription.new
  yield(a)

  i = a.identifier

  trace_file = "#{SUBJECTS_OUT}/#{i}-trace.xml"

  terminal_tasks = [:"#{i}_check"]

  java :"#{i}_trace" => :prepare_subjects do |t|
    t.classname = a.system_test
    t.premain_agent = AMOCK_JAR
    t.premain_options = "--tracefile=#{trace_file}"
  end

  junit :"#{i}_check" => :"#{i}_trace" do |t|
    t.suite = a.system_test + "$ProcessorTests"
  end

  a.unit_tests.each do |u|
    id = "#{i}-#{u.identifier}"
    unit_test_file = "#{SUBJECTS_OUT}/#{u.unit_test}.java"

  
    java :"#{id}_process" => :"#{i}_trace" do |t|
      t.classname = amock_class('processor.Processor')
      t.args << trace_file
      t.args << unit_test_file
      t.args << u.unit_test
      t.args << u.test_method
      t.args << u.tested_class
    end

    javac :"#{id}_compile" => :"#{id}_process" do |t|
      t.sources = [unit_test_file]
      t.destination = SUBJECTS_BIN
    end

    junit :"#{id}_try" => :"#{id}_compile" do |t|
      t.suite = amock_class("subjects.generated.#{u.unit_test}")
    end
    
    terminal_tasks << "#{id}_try"
  end

  task i.to_sym => terminal_tasks
end

amock_test do |a|
  a.system_test = amock_class('subjects.bakery.Bakery')
  a.identifier = :bakery

  a.unit_test do |u|
    u.identifier = 'cm'
    u.unit_test = 'AutoCookieMonsterTest'
    u.test_method = "cookieEating"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/CookieMonster"
  end

  a.unit_test do |u|
    u.identifier = 'ncm'
    u.unit_test = 'AutoNamedCookieMonsterTest'
    u.test_method = "cookieEating"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/NamedCookieMonster"
  end

  a.unit_test do |u|
    u.identifier = 'vcm'
    u.unit_test = 'AutoVoidingCookieMonsterTest'
    u.test_method = "cookieEating"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/VoidingCookieMonster"
  end
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
