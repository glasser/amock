require 'build/amock_tasks'

# Actually define tests.

amock_test do |a|
  a.system_test = amock_class('subjects.bakery.Bakery')
  a.identifier = :bakery

  a.unit_test do |u|
    u.identifier = 'cm'
    u.unit_test = 'AutoCookieMonsterTest'
    u.test_method = "cookieEating"
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "CookieMonster"
  end

  a.unit_test do |u|
    u.identifier = 'ncm'
    u.unit_test = 'AutoNamedCookieMonsterTest'
    u.test_method = "cookieEating"
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "NamedCookieMonster"
  end

  a.unit_test do |u|
    u.identifier = 'vcm'
    u.unit_test = 'AutoVoidingCookieMonsterTest'
    u.test_method = "cookieEating"
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "VoidingCookieMonster"
  end

  a.unit_test do |u|
    u.identifier = 'cj'
    u.unit_test = 'AutoCookieJarTest'
    u.test_method = "cookieEating"
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "CookieJar"
  end

  a.unit_test do |u|
    u.identifier = 'oc'
    u.unit_test = 'AutoOatmealCookieTest'
    u.test_method = "cookieEating"
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "OatmealCookie"
  end

  a.unit_test do |u|
    u.identifier = 'refl'
    u.unit_test = 'AutoReflectedCookieMonsterTest'
    u.test_method = 'reflectedCookieEating'
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = 'ReflectedCookieMonster'
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeMock')
  a.identifier = :fields_mock

  a.unit_test do |u|
    u.identifier = 'patron'
    u.unit_test = 'AutoPatronTest'
    u.test_method = "patronizing"
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "Patron"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeRP')
  a.identifier = :fields_rp

  a.unit_test do |u|
    u.identifier = 'patron'
    u.unit_test = 'AutoPatronTest'
    u.test_method = "patronizing"
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "Patron"
  end
end

task :fields => [:fields_mock, :fields_rp]

amock_test do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystem')
  a.identifier = :rect

  a.unit_test do |u|
    u.identifier = 'rect-no-tweak'
    u.unit_test = 'AutoRectangleTest'
    u.test_method = "rectifying"
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "RectangleHelper"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.RectangleSystemTweak')
  a.identifier = :rect_tweak

  a.unit_test do |u|
    u.identifier = 'rect-tweak'
    u.unit_test = 'AutoRectangleTweakTest'
    u.test_method = "rectifyingTweakily"
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = "RectangleHelper"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.StaticFieldSystem')
  a.identifier = :staticfield
  
  a.unit_test do |u|
    u.identifier = 'get'
    u.unit_test = 'AutoSFTest'
    u.test_method = 'fetching'
    u.package = 'edu.mit.csail.pag.amock.subjects.fields'
    u.tested_class = 'StaticFieldSystem'
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.hierarchy.HierarchySystem')
  a.identifier = :hierarchy

  a.unit_test do |u|
    u.identifier = 'hs'
    u.unit_test = 'AutoHierarchyTest'
    u.test_method = "hiering"
    u.package = 'edu.mit.csail.pag.amock.subjects.hierarchy'
    u.tested_class = "HierarchySystem"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.staticmethod.SmockSystem')
  a.identifier = :static

  a.unit_test do |u|
    u.identifier = 's'
    u.unit_test = 'AutoSmockTest'
    u.test_method = "smocking"
    u.package = 'edu.mit.csail.pag.amock.subjects.staticmethod'
    u.tested_class = "SmockSystem"
  end
end

amock_test do |a|
  a.system_test = 'org.tmatesoft.svn.cli.SVN'
  a.args << 'ls'
  a.args << 'http://svn.collab.net/repos/svn'
#  a.args << 'file:///Users/glasser/Scratch/repo'
  a.identifier = :svnkit

#   a.unit_test do |u|
#     u.identifier = 'wcclientmanager'
#     u.unit_test = 'AutoCMTest'
#     u.test_method = "clienting"
#     u.tested_class = "SVNClientManager"
#   end

  a.unit_test do |u|
    u.identifier = :logclient
    u.unit_test = 'AutoLogClientTest'
    u.test_method = "logging"
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = "SVNLogClient"
  end

  a.unit_test do |u|
    u.identifier = 'lscommand'
    u.unit_test = 'AutoCommandTest'
    u.test_method = "listing"
    u.package = "org.tmatesoft.svn.cli"
    u.tested_class = "org.tmatesoft.svn.cli.command.SVNLsCommand"
  end
end

JMODELLER_RAW_TRACE = "subjects/in/jmodeller/sample-raw.xml"
JMODELLER_TRACE = "subjects/in/jmodeller/sample.xml"
JMODELLER_HIERARCHY = "subjects/in/jmodeller/hierarchy.xml"
JMODELLER_II = "subjects/in/jmodeller/ii.xml"

gunzip JMODELLER_TRACE
gunzip JMODELLER_RAW_TRACE
gunzip JMODELLER_II

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_generate_by_hand => [AMOCK_JAR, SUBJECTS_OUT] do |t|
  t.classname = "JModellerApplication"
  t.premain_agent = AMOCK_JAR
  t.premain_options = "--tracefile=#{JMODELLER_RAW_TRACE},--hierarchyfile=#{JMODELLER_HIERARCHY}"
end

# nb: make sure, after doing this, to gzip and check that version in!
java :jmodeller_fix => JMODELLER_RAW_TRACE do |t|
  t.classname = amock_class('trace.ConstructorFixer')
  t.args << JMODELLER_RAW_TRACE
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
  u.unit_test = 'JModellerTest'
  u.test_method = 'modelling'
  u.package = 'CH.ifa.draw.standard'
  u.tested_class = 'ConnectionTool'
end
