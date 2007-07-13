require 'build/amock_tasks'

# Actually define tests.

amock_test(:bakery) do |a|
  a.system_test = amock_class('subjects.bakery.Bakery')

  a.unit_test('cm') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "CookieMonster"
  end

  a.unit_test('ncm') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "NamedCookieMonster"
  end

  a.unit_test('vcm') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "VoidingCookieMonster"
  end

  a.unit_test('cj') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "CookieJar"
  end

  a.unit_test('oc') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "OatmealCookie"
  end

  a.unit_test('refl') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = 'ReflectedCookieMonster'
  end
end

amock_test(:fields_mock) do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeMock')

  a.unit_test('patron') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "Patron"
  end
end

amock_test(:fields_rp) do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeRP')

  a.unit_test('patron') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "Patron"
  end
end

task :fields => [:fields_mock, :fields_rp]

amock_test(:rect) do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystem')

  a.unit_test('rect') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "RectangleHelper"
  end
end

amock_test(:rect_tweak) do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystemTweak')

  a.unit_test('rect') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "RectangleHelper"
  end
end

amock_test(:staticfield) do |a|
  a.system_test = amock_class('subjects.fields.StaticFieldSystem')
  
  a.unit_test('get') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = 'StaticFieldSystem'
  end
end

amock_test(:hierarchy) do |a|
  a.system_test = amock_class('subjects.hierarchy.HierarchySystem')

  a.unit_test('hs') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.hierarchy'
    u.tested_class = "HierarchySystem"
  end
end

amock_test(:static) do |a|
  a.system_test = amock_class('subjects.staticmethod.SmockSystem')

  a.unit_test('s') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.staticmethod'
    u.tested_class = "SmockSystem"
  end
end

amock_test(:capture) do |a|
  a.system_test = amock_class('subjects.capture.CaptureSystem')

  a.unit_test('receiverclient') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.capture'
    u.tested_class = "ReceiverClient"
  end

  a.unit_test('rqclient') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.capture'
    u.tested_class = "ReceiverQuickClient"
  end

  a.unit_test('bouncerclient') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.capture'
    u.tested_class = "BouncerClient"
  end

  a.unit_test('bqclient') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.capture'
    u.tested_class = "BouncerQuickClient"
  end
end

amock_test(:joke) do |a|
  a.system_test = amock_class('subjects.callback.JokeSystem')

  a.unit_test('audience') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.callback'
    u.tested_class = "JokeAudience"
  end

  a.unit_test('teller') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.callback'
    u.tested_class = "JokeTeller"
  end
end

amock_test(:jdkmethodentry) do |a|
  a.system_test = amock_class('subjects.callback.JDKSystem')

  a.unit_test(:cb) do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.callback'
    u.tested_class = "JDKSystem"
  end
end

amock_test(:svnkit) do |a|
  a.system_test = 'org.tmatesoft.svn.cli.SVN'
  a.args << 'ls'
  a.args << 'http://svn.collab.net/repos/svn'
#  a.args << 'file:///Users/glasser/Scratch/repo'

  a.unit_test('logclient') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = "SVNLogClient"
  end

  a.unit_test('lscommand') do |u|
    u.package = "org.tmatesoft.svn.cli"
    u.tested_class = "org.tmatesoft.svn.cli.command.SVNLsCommand"
  end

  a.unit_test('wcclientmanager') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = "SVNClientManager"
  end
end

amock_test(:eclipsec) do |a|
  a.system_test = 'org.eclipse.jdt.internal.compiler.batch.Main'
  a.args << '-d'
  a.args << 'none'
  a.args << 'subjects/in/eclipsec/HelloWorld.java'

  a.unit_test('cud') do |u|
    u.package = 'org.eclipse.jdt.internal.compiler.ast'
    u.tested_class = 'CompilationUnitDeclaration'
  end

  a.unit_test('compiler') do |u|
    u.package = 'org.eclipse.jdt.internal.compiler'
    u.tested_class = "Compiler"
  end
end

JMODELLER_RAW_TRACE = "subjects/in/jmodeller/sample-raw.xml"
JMODELLER_TRIMMED_TRACE = "subjects/in/jmodeller/sample-trim.xml"
JMODELLER_FIXED_TRACE = "subjects/in/jmodeller/sample-fixed.xml"
JMODELLER_TRACE = "subjects/in/jmodeller/sample.xml"
JMODELLER_HIERARCHY = "subjects/in/jmodeller/hierarchy.xml"
JMODELLER_II = "subjects/in/jmodeller/ii.xml"

gunzip JMODELLER_RAW_TRACE
gunzip JMODELLER_TRIMMED_TRACE
gunzip JMODELLER_FIXED_TRACE
gunzip JMODELLER_TRACE
gunzip JMODELLER_II

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_generate_by_hand => [AMOCK_JAR, SUBJECTS_OUT] do |t|
  t.classname = "JModellerApplication"
  t.premain_agent = AMOCK_JAR
  t.premain_options = "--tracefile=#{JMODELLER_RAW_TRACE},--hierarchyfile=#{JMODELLER_HIERARCHY}"
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_trim => JMODELLER_RAW_TRACE do |t|
  t.classname = amock_class('trace.ClinitTrimmer')
  t.args << JMODELLER_RAW_TRACE
  t.args << JMODELLER_TRIMMED_TRACE
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_fix => JMODELLER_TRIMMED_TRACE do |t|
  t.classname = amock_class('trace.ConstructorFixer')
  t.args << JMODELLER_TRIMMED_TRACE
  t.args << JMODELLER_FIXED_TRACE
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_analyze => JMODELLER_FIXED_TRACE do |t|
  t.classname = amock_class('processor.AnalyzeMethodEntry')
  t.args << JMODELLER_FIXED_TRACE
  t.args << JMODELLER_TRACE
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_ii => [JMODELLER_TRACE, :build] do |t|
  t.classname = amock_class('processor.GatherInstanceInfo')
  t.args << JMODELLER_TRACE
  t.args << JMODELLER_II
end


# maybe should depend on JMODELLER_HIERARCHY too?  it's not zipped
# though
unit_test(:jmodeller, JMODELLER_TRACE, JMODELLER_II, JMODELLER_HIERARCHY,
          [JMODELLER_II, JMODELLER_TRACE]) do |u|
  u.package = 'CH.ifa.draw.standard'
  u.tested_class = 'ConnectionTool'
end
