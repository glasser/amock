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

# rect and rect_tweak temporarily removed
task :default => [:clean, :check, :bakery, :fields]



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

  raw_trace_file = "#{SUBJECTS_OUT}/#{i}-trace-raw.xml"
  trace_file = "#{SUBJECTS_OUT}/#{i}-trace.xml"

  terminal_tasks = [:"#{i}_check"]

  java :"#{i}_trace" => :prepare_subjects do |t|
    t.classname = a.system_test
    t.premain_agent = AMOCK_JAR
    t.premain_options = "--tracefile=#{raw_trace_file}"
  end

  java :"#{i}_fix" => :"#{i}_trace" do |t|
    t.classname = amock_class('trace.ConstructorFixer')
    t.args << raw_trace_file
    t.args << trace_file
  end

  junit :"#{i}_check" => :"#{i}_fix" do |t|
    t.suite = a.system_test + "$ProcessorTests"
  end

  a.unit_tests.each do |u|
    id = "#{i}-#{u.identifier}"
 
    define_unit_test(u, id, trace_file, [:"#{i}_fix"])

    terminal_tasks << "#{id}_try"
  end

  task i.to_sym => terminal_tasks
end

def define_unit_test(u, id, trace_file, prereq)
  unit_test_file = "#{SUBJECTS_OUT}/#{u.unit_test}.java"
  tcg_dump = "#{SUBJECTS_OUT}/tcg-#{u.unit_test}.xml"
  tcg_dump1 = "#{SUBJECTS_OUT}/tcg1-#{u.unit_test}.xml"
  rp_dump = "#{SUBJECTS_OUT}/rp-#{u.unit_test}.xml"

  java :"#{id}_irp" => prereq+[:prepare_subjects] do |t|
    t.classname = amock_class('processor.IdentifyRecordPrimaries')
    t.args << trace_file
    t.args << rp_dump
  end
  
  java :"#{id}_process" => :"#{id}_irp" do |t|
    t.classname = amock_class('processor.Processor')
    t.args << trace_file
    t.args << tcg_dump
    t.args << rp_dump
    t.args << u.unit_test
    t.args << u.test_method
    t.args << u.tested_class
  end

  java :"#{id}_dumd" => :"#{id}_process" do |t|
    t.classname = amock_class('processor.DetectUnnecessaryDeclarations')
    t.args << tcg_dump
    t.args << tcg_dump1
  end

  java :"#{id}_sourcify" => :"#{id}_dumd" do |t|
    t.classname = amock_class('representation.Sourcify')
    t.args << tcg_dump1
    t.args << unit_test_file
  end

  javac :"#{id}_compile" => :"#{id}_sourcify" do |t|
    t.sources = [unit_test_file]
    t.destination = SUBJECTS_BIN
  end

  junit :"#{id}_try" => :"#{id}_compile" do |t|
    t.suite = u.unit_test
  end
end

def unit_test(id, trace_file, prereq)
  u = UnitTestDescription.new
  yield(u)
  define_unit_test(u, id, trace_file, prereq)
end



# Actually define tests.

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

  a.unit_test do |u|
    u.identifier = 'cj'
    u.unit_test = 'AutoCookieJarTest'
    u.test_method = "cookieEating"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/CookieJar"
  end

  a.unit_test do |u|
    u.identifier = 'oc'
    u.unit_test = 'AutoOatmealCookieTest'
    u.test_method = "cookieEating"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/bakery/OatmealCookie"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem')
  a.identifier = :fields

  a.unit_test do |u|
    u.identifier = 'patron'
    u.unit_test = 'AutoPatronTest'
    u.test_method = "patronizing"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/fields/Patron"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystem')
  a.identifier = :rect

  a.unit_test do |u|
    u.identifier = 'rect-no-tweak'
    u.unit_test = 'AutoRectangleTest'
    u.test_method = "rectifying"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/fields/RectangleHelper"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystemTweak')
  a.identifier = :rect_tweak

  a.unit_test do |u|
    u.identifier = 'rect-tweak'
    u.unit_test = 'AutoRectangleTweakTest'
    u.test_method = "rectifyingTweakily"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/fields/RectangleHelper"
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
  t.suite = 'CookieMonsterTest'
end

task :check_system => [:run_cookie_eating]

task :check => [:check_unit, :check_system]

JMODELLER_RAW_TRACE = "notes/jmodeller-sample-raw.xml"
JMODELLER_TRACE = "notes/jmodeller-sample.xml"
JMODELLER_JUNIT = "#{SUBJECTS_OUT}/JModellerTest.java"

gunzip JMODELLER_TRACE
gunzip JMODELLER_RAW_TRACE

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_generate_by_hand => [AMOCK_JAR, SUBJECTS_OUT] do |t|
  t.classname = "JModellerApplication"
  t.premain_agent = AMOCK_JAR
  t.premain_options = "--tracefile=#{JMODELLER_RAW_TRACE}"
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_fix => JMODELLER_RAW_TRACE do |t|
  t.classname = amock_class('trace.ConstructorFixer')
  t.args << JMODELLER_RAW_TRACE
  t.args << JMODELLER_TRACE
end

unit_test(:jmodeller, JMODELLER_TRACE, [JMODELLER_TRACE]) do |u|
  u.unit_test = 'JModellerTest'
  u.test_method = 'modelling'
  u.tested_class = 'CH/ifa/draw/standard/ConnectionTool'
end

# You can set env variables at the command line: 
#  $ rake rpci CLASS=edu/mit/csail/pag/amock/subjects/fields/Book
java :rpci => [:build, :build_subjects] do |t|
  t.classname = amock_class('representation.RecordPrimaryClassInfo')
  t.args << ENV["CLASS"]
end
