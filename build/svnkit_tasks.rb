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

  a.unit_test('s1') do |u|
    u.package = 'org.tmatesoft.svn.cli'
    u.tested_class = 'SVNCommandLine'
  end

  a.unit_test('s2') do |u|
    u.package = 'org.tmatesoft.svn.cli'
    u.tested_class = 'SVNConsoleAuthenticationProvider'
  end

  a.unit_test('s3') do |u|
    u.package = 'org.tmatesoft.svn.cli.command'
    u.tested_class = 'SVNLsCommand'
  end

  a.unit_test('s4') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNDirEntry'
  end

  a.unit_test('s5') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNErrorCode'
  end

  a.unit_test('s6') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNErrorMessage'
  end

  a.unit_test('s7') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNNodeKind'
  end

  a.unit_test('s8') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNURL'
  end

  a.unit_test('s9') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVBaselineInfo'
  end

  a.unit_test('s10') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVConnection'
  end

  a.unit_test('s11') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVElement'
  end

  a.unit_test('s12') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVProperties'
  end

  a.unit_test('s13') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVRepository'
  end

  a.unit_test('s14') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVRepositoryFactory'
  end

  a.unit_test('s15') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.handlers'
    u.tested_class = 'DAVPropertiesHandler'
  end

  a.unit_test('s16') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'FixedSizeInputStream'
  end

  a.unit_test('s17') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPConnection'
  end

  a.unit_test('s18') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPHeader'
  end

  a.unit_test('s19') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPRequest'
  end

  a.unit_test('s20') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPStatus'
  end

  a.unit_test('s21') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'XMLReader'
  end

  a.unit_test('s22') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.fs'
    u.tested_class = 'FSRepositoryFactory'
  end

  a.unit_test('s23') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.svn'
    u.tested_class = 'SVNRepositoryFactoryImpl'
  end

  a.unit_test('s24') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.util'
    u.tested_class = 'DefaultSVNDebugLogger'
  end

  a.unit_test('s25') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.util'
    u.tested_class = 'SVNDate'
  end

  a.unit_test('s26') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'DefaultSVNAuthenticationManager'
  end

  a.unit_test('s27') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'DefaultSVNOptions'
  end

  a.unit_test('s28') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'SVNCompositeConfigFile'
  end

  a.unit_test('s29') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'SVNConfigFile'
  end

  a.unit_test('s30') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'DefaultSVNRepositoryPool'
  end

  a.unit_test('s31') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNClientManager'
  end

  a.unit_test('s32') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNLogClient'
  end

  a.unit_test('s33') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNRevision'
  end

# t is from the sensitivity expt
  a.unit_test('t1') do |u|
    u.package = 'org.tmatesoft.svn.cli'
    u.tested_class = 'SVNCommandLine'
  end

  a.unit_test('t2') do |u|
    u.package = 'org.tmatesoft.svn.cli.command'
    u.tested_class = 'LsCommand'
  end

  a.unit_test('t3') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNDirEntry'
  end

  a.unit_test('t4') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNErrorCode'
  end

  a.unit_test('t5') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNErrorMessage'
  end

  a.unit_test('t6') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNNodeKind'
  end

  a.unit_test('t7') do |u|
    u.package = 'org.tmatesoft.svn.core'
    u.tested_class = 'SVNURL'
  end

  a.unit_test('t8') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVBaselineInfo'
  end

  a.unit_test('t9') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVConnection'
  end

  a.unit_test('t10') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVElement'
  end

  a.unit_test('t11') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVProperties'
  end

  a.unit_test('t12') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVRepository'
  end

  a.unit_test('t13') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav'
    u.tested_class = 'DAVRepositoryFactory'
  end

  a.unit_test('t14') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.handlers'
    u.tested_class = 'DAVPropertiesHandler'
  end

  a.unit_test('t15') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'FixedSizeInputStream'
  end

  a.unit_test('t16') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPConnection'
  end

  a.unit_test('t17') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPHeader'
  end

  a.unit_test('t18') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPRequest'
  end

  a.unit_test('t19') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'HTTPStatus'
  end

  a.unit_test('t20') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.dav.http'
    u.tested_class = 'XMLReader'
  end

  a.unit_test('t21') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.fs'
    u.tested_class = 'FSRepositoryFactory'
  end

  a.unit_test('t22') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.io.svn'
    u.tested_class = 'SVNRepositoryFactoryImpl'
  end

  a.unit_test('t23') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.util'
    u.tested_class = 'DefaultSVNDebugLogger'
  end

  a.unit_test('t24') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'DefaultSVNAuthenticationManager'
  end

  a.unit_test('t25') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'DefaultSVNOptions'
  end

  a.unit_test('t26') do |u|
    u.package = 'org.tmatesoft.svn.core.internal.wc'
    u.tested_class = 'SVNConfigFile'
  end

  a.unit_test('t27') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'DefaultSVNRepositoryPool'
  end

  a.unit_test('t28') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNClientManager'
  end

  a.unit_test('t29') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNLogClient'
  end

  a.unit_test('t30') do |u|
    u.package = 'org.tmatesoft.svn.core.wc'
    u.tested_class = 'SVNRevision'
  end
end
