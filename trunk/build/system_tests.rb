require 'build/amock_test'

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

  a.unit_test do |u|
    u.identifier = 'refl'
    u.unit_test = 'AutoReflectedCookieMonsterTest'
    u.test_method = 'reflectedCookieEating'
    u.tested_class = 'edu/mit/csail/pag/amock/subjects/bakery/ReflectedCookieMonster'
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeMock')
  a.identifier = :fields_mock

  a.unit_test do |u|
    u.identifier = 'patron'
    u.unit_test = 'AutoPatronTest'
    u.test_method = "patronizing"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/fields/Patron"
  end
end

amock_test do |a|
  a.system_test = amock_class('subjects.fields.FieldSystem$MakeRP')
  a.identifier = :fields_rp

  a.unit_test do |u|
    u.identifier = 'patron'
    u.unit_test = 'AutoPatronTest'
    u.test_method = "patronizing"
    u.tested_class = "edu/mit/csail/pag/amock/subjects/fields/Patron"
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

amock_test do |a|
  a.system_test = 'org.tmatesoft.svn.cli.SVN'
  a.args << 'ls'
  a.args << 'http://svn.collab.net/repos/svn'
  a.identifier = :svnkit

#   a.unit_test do |u|
#     u.identifier = 'wcclientmanager'
#     u.unit_test = 'AutoCMTest'
#     u.test_method = "clienting"
#     u.tested_class = "org/tmatesoft/svn/core/wc/SVNClientManager"
#   end

#   a.unit_test do |u|
#     u.identifier = 'logclient'
#     u.unit_test = 'AutoLogClientTest'
#     u.test_method = "logging"
#     u.tested_class = "org/tmatesoft/svn/core/wc/SVNLogClient"
#   end

  a.unit_test do |u|
    u.identifier = 'lscommand'
    u.unit_test = 'AutoCommandTest'
    u.test_method = "listing"
    u.tested_class = "org/tmatesoft/svn/cli/command/SVNLsCommand"
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
  u.tested_class = 'CH/ifa/draw/standard/ConnectionTool'
end
