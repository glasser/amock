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

  output_dir = "#{SUBJECTS_OUT}/#{i}"
  directory output_dir

  raw_trace_file = "#{output_dir}/trace-raw.xml"
  trace_file = "#{output_dir}/trace.xml"
  instinfo_file = "#{output_dir}/ii.xml"

  java :"#{i}_trace" => [:prepare_subjects, output_dir] do |t|
    t.classname = a.system_test
    t.premain_agent = AMOCK_JAR
    t.premain_options = "--tracefile=#{raw_trace_file}"
  end

  java :"#{i}_fix" => :"#{i}_trace" do |t|
    t.classname = amock_class('trace.ConstructorFixer')
    t.args << raw_trace_file
    t.args << trace_file
  end

  java :"#{i}_ii" => :"#{i}_fix" do |t|
    t.classname = amock_class('processor.GatherInstanceInfo')
    t.args << trace_file
    t.args << instinfo_file
  end

  sub_unit_tests = []

  a.unit_tests.each do |u|
    id = "#{i}-#{u.identifier}"
    unit_output_dir = "#{output_dir}/#{u.identifier}"
 
    define_unit_test(u, id, unit_output_dir, trace_file, instinfo_file, [:"#{i}_ii"])

    sub_unit_tests << "#{id}_try"
  end

  junit :"#{i}_check" => sub_unit_tests+[:"#{i}_ii"] do |t|
    t.suite = a.system_test + "$ProcessorTests"
    t.env["AMOCK_TRACE_FILE"] = trace_file
    t.env["AMOCK_INSTINFO_FILE"] = instinfo_file
  end

  task i.to_sym => (sub_unit_tests+[:"#{i}_check"])
end

def define_unit_test(u, id, output_dir, trace_file, instinfo_file, prereq)
  unit_test_file = "#{output_dir}/#{u.unit_test}.java"
  tcg_dump = "#{output_dir}/tcg.xml"
  tcg_dump1 = "#{output_dir}/tcg1.xml"

  directory output_dir

  java :"#{id}_process" => prereq+[:prepare_subjects, output_dir] do |t|
    t.classname = amock_class('processor.Processor')
    t.args << trace_file
    t.args << tcg_dump
    t.args << instinfo_file
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

def unit_test(id, trace_file, instinfo_file, prereq)
  u = UnitTestDescription.new
  yield(u)

  output_dir = "#{SUBJECTS_OUT}/#{id}"

  define_unit_test(u, id, output_dir,
                   trace_file, instinfo_file, prereq)
end
