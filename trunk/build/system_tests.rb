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

  a.unit_test('ecm') do |u|
    u.package = 'edu.mit.csail.pag.amock.subjects.bakery'
    u.tested_class = "EagerCookieMonster"
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

amock_test(:esper) do |a|
  a.system_test = 'net.esper.example.transaction.sim.TxnGenMain'
  a.args << 'tiniest'
  a.args << '10'

  a.vm_args << '-Dlog4j.configuration=log4j.xml'

  a.unit_test('epr') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPRuntimeImpl'
  end

  a.unit_test('a1') do |u|
    u.package = 'net.esper.client'
    u.tested_class = 'Configuration'
  end

  a.unit_test('a2') do |u|
    u.package = 'net.esper.client'
    u.tested_class = 'ConfigurationEngineDefaults'
  end

  a.unit_test('a3') do |u|
    u.package = 'net.esper.client'
    u.tested_class = 'EPStatementState'
  end

  a.unit_test('a4') do |u|
    u.package = 'net.esper.client.time'
    u.tested_class = 'CurrentTimeEvent'
  end

  a.unit_test('a5') do |u|
    u.package = 'net.esper.client.time'
    u.tested_class = 'TimerControlEvent'
  end

  a.unit_test('a6') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'ArrayBackedCollection'
  end

  a.unit_test('a7') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'FlushedEventBuffer'
  end

  a.unit_test('a8') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'MultiKey'
  end

  a.unit_test('a9') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'MultiKeyUntyped'
  end

  a.unit_test('a10') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'NumberSetPermutationEnumeration'
  end

  a.unit_test('a11') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'Pair'
  end

  a.unit_test('a12') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'PermutationEnumeration'
  end

  a.unit_test('a13') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'RefCountedMap'
  end

  a.unit_test('a14') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'SortedRefCountedSet'
  end

  a.unit_test('a15') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'ThreadWorkQueue'
  end

  a.unit_test('a16') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'TimeWindow'
  end

  a.unit_test('a17') do |u|
    u.package = 'net.esper.collection'
    u.tested_class = 'UniformPair'
  end

  a.unit_test('a18') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'ConfigurationOperationsImpl'
  end

  a.unit_test('a19') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'ConfigurationSnapshot'
  end

  a.unit_test('a20') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'DispatchFuture'
  end

  a.unit_test('a21') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPAdministratorImpl'
  end

  a.unit_test('a22') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPRuntimeImpl'
  end

  a.unit_test('a23') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPServiceProviderImpl'
  end

  a.unit_test('a24') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPServicesContext'
  end

  a.unit_test('a25') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPServicesContextFactoryDefault'
  end

  a.unit_test('a26') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPStatementHandle'
  end

  a.unit_test('a27') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPStatementHandleCallback'
  end

  a.unit_test('a28') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPStatementImpl'
  end

  a.unit_test('a29') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPStatementListenerSet'
  end

  a.unit_test('a30') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EPStatementStartMethod'
  end

  a.unit_test('a31') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'EngineEnvContext'
  end

  a.unit_test('a32') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'StatementContext'
  end

  a.unit_test('a33') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'StatementContextFactoryDefault'
  end

  a.unit_test('a34') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'StatementLifecycleSvcImpl'
  end

  a.unit_test('a35') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'StatementLockFactoryImpl'
  end

  a.unit_test('a36') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'SubSelectStreamCollection'
  end

  a.unit_test('a37') do |u|
    u.package = 'net.esper.core'
    u.tested_class = 'UpdateDispatchViewBlocking'
  end

  a.unit_test('a38') do |u|
    u.package = 'net.esper.dispatch'
    u.tested_class = 'DispatchServiceImpl'
  end

  a.unit_test('a39') do |u|
    u.package = 'net.esper.emit'
    u.tested_class = 'EmitServiceImpl'
  end

  a.unit_test('a40') do |u|
    u.package = 'net.esper.eql.agg'
    u.tested_class = 'AggregationServiceGroupAllImpl'
  end

  a.unit_test('a41') do |u|
    u.package = 'net.esper.eql.agg'
    u.tested_class = 'AggregationServiceGroupByImpl'
  end

  a.unit_test('a42') do |u|
    u.package = 'net.esper.eql.agg'
    u.tested_class = 'AggregationServiceNull'
  end

  a.unit_test('a43') do |u|
    u.package = 'net.esper.eql.agg'
    u.tested_class = 'AvgAggregator'
  end

  a.unit_test('a44') do |u|
    u.package = 'net.esper.eql.agg'
    u.tested_class = 'MinMaxAggregator'
  end

  a.unit_test('a45') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'EngineImportServiceImpl'
  end

  a.unit_test('a46') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'EngineSettingsService'
  end

  a.unit_test('a47') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'MethodResolutionServiceImpl'
  end

  a.unit_test('a48') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'PropertyResolutionDescriptor'
  end

  a.unit_test('a49') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'ResultSetProcessorRowForAll'
  end

  a.unit_test('a50') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'ResultSetProcessorRowPerGroup'
  end

  a.unit_test('a51') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'ResultSetProcessorSimple'
  end

  a.unit_test('a52') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'SelectExprEvalProcessor'
  end

  a.unit_test('a53') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'SelectExprJoinWildcardProcessor'
  end

  a.unit_test('a54') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'StreamTypeServiceImpl'
  end

  a.unit_test('a55') do |u|
    u.package = 'net.esper.eql.core'
    u.tested_class = 'ViewResourceDelegateImpl'
  end

  a.unit_test('a56') do |u|
    u.package = 'net.esper.eql.db'
    u.tested_class = 'DatabaseConfigServiceImpl'
  end

  a.unit_test('a57') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprAndNode'
  end

  a.unit_test('a58') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprAvgNode'
  end

  a.unit_test('a59') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprConstantNode'
  end

  a.unit_test('a60') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprEqualsNode'
  end

  a.unit_test('a61') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprIdentNode'
  end

  a.unit_test('a62') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprMathNode'
  end

  a.unit_test('a63') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprMinMaxAggrNode'
  end

  a.unit_test('a64') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprNodeIdentifierVisitor'
  end

  a.unit_test('a65') do |u|
    u.package = 'net.esper.eql.expression'
    u.tested_class = 'ExprNodeSubselectVisitor'
  end

  a.unit_test('a66') do |u|
    u.package = 'net.esper.eql.generated'
    u.tested_class = 'EQLStatementLexer'
  end

  a.unit_test('a67') do |u|
    u.package = 'net.esper.eql.generated'
    u.tested_class = 'EQLStatementParser'
  end

  a.unit_test('a68') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'ExecNodeQueryStrategy'
  end

  a.unit_test('a69') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'JoinExecStrategyDispatchable'
  end

  a.unit_test('a70') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'JoinExecutionStrategyImpl'
  end

  a.unit_test('a71') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'JoinSetComposerFactoryImpl'
  end

  a.unit_test('a72') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'JoinSetComposerImpl'
  end

  a.unit_test('a73') do |u|
    u.package = 'net.esper.eql.join'
    u.tested_class = 'JoinSetFilter'
  end

  a.unit_test('a74') do |u|
    u.package = 'net.esper.eql.join.assemble'
    u.tested_class = 'BranchOptionalAssemblyNode'
  end

  a.unit_test('a75') do |u|
    u.package = 'net.esper.eql.join.assemble'
    u.tested_class = 'LeafAssemblyNode'
  end

  a.unit_test('a76') do |u|
    u.package = 'net.esper.eql.join.assemble'
    u.tested_class = 'RootCartProdAssemblyNode'
  end

  a.unit_test('a77') do |u|
    u.package = 'net.esper.eql.join.assemble'
    u.tested_class = 'RootOptionalAssemblyNode'
  end

  a.unit_test('a78') do |u|
    u.package = 'net.esper.eql.join.exec'
    u.tested_class = 'IndexedTableLookupStrategy'
  end

  a.unit_test('a79') do |u|
    u.package = 'net.esper.eql.join.exec'
    u.tested_class = 'LookupInstructionExec'
  end

  a.unit_test('a80') do |u|
    u.package = 'net.esper.eql.join.exec'
    u.tested_class = 'LookupInstructionExecNode'
  end

  a.unit_test('a81') do |u|
    u.package = 'net.esper.eql.join.exec'
    u.tested_class = 'NestedIterationExecNode'
  end

  a.unit_test('a82') do |u|
    u.package = 'net.esper.eql.join.exec'
    u.tested_class = 'TableLookupExecNode'
  end

  a.unit_test('a83') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'IndexedTableLookupPlan'
  end

  a.unit_test('a84') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'LookupInstructionPlan'
  end

  a.unit_test('a85') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'LookupInstructionQueryPlanNode'
  end

  a.unit_test('a86') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'NestedIterationNode'
  end

  a.unit_test('a87') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'OuterInnerDirectionalGraph'
  end

  a.unit_test('a88') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'QueryGraph'
  end

  a.unit_test('a89') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'QueryGraphKey'
  end

  a.unit_test('a90') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'QueryGraphValue'
  end

  a.unit_test('a91') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'QueryPlan'
  end

  a.unit_test('a92') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'QueryPlanIndex'
  end

  a.unit_test('a93') do |u|
    u.package = 'net.esper.eql.join.plan'
    u.tested_class = 'TableLookupNode'
  end

  a.unit_test('a94') do |u|
    u.package = 'net.esper.eql.join.rep'
    u.tested_class = 'Cursor'
  end

  a.unit_test('a95') do |u|
    u.package = 'net.esper.eql.join.rep'
    u.tested_class = 'Node'
  end

  a.unit_test('a96') do |u|
    u.package = 'net.esper.eql.join.rep'
    u.tested_class = 'NodeCursorIterator'
  end

  a.unit_test('a97') do |u|
    u.package = 'net.esper.eql.join.rep'
    u.tested_class = 'RepositoryImpl'
  end

  a.unit_test('a98') do |u|
    u.package = 'net.esper.eql.join.rep'
    u.tested_class = 'SingleCursorIterator'
  end

  a.unit_test('a99') do |u|
    u.package = 'net.esper.eql.join.table'
    u.tested_class = 'PropertyIndexedEventTable'
  end

  a.unit_test('a100') do |u|
    u.package = 'net.esper.eql.parse'
    u.tested_class = 'EQLTreeWalker'
  end

  a.unit_test('a101') do |u|
    u.package = 'net.esper.eql.parse'
    u.tested_class = 'TimePeriodParameter'
  end

  a.unit_test('a102') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'FilterSpecRaw'
  end

  a.unit_test('a103') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'FilterStreamSpecCompiled'
  end

  a.unit_test('a104') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'FilterStreamSpecRaw'
  end

  a.unit_test('a105') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'InsertIntoDesc'
  end

  a.unit_test('a106') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'OuterJoinDesc'
  end

  a.unit_test('a107') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'PluggableObjectCollection'
  end

  a.unit_test('a108') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'PluggableObjectType'
  end

  a.unit_test('a109') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'SelectClauseSpec'
  end

  a.unit_test('a110') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'SelectClauseStreamSelectorEnum'
  end

  a.unit_test('a111') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'SelectExprElementCompiledSpec'
  end

  a.unit_test('a112') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'SelectExprElementRawSpec'
  end

  a.unit_test('a113') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'StatementSpecCompiled'
  end

  a.unit_test('a114') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'StatementSpecRaw'
  end

  a.unit_test('a115') do |u|
    u.package = 'net.esper.eql.spec'
    u.tested_class = 'ViewSpec'
  end

  a.unit_test('a116') do |u|
    u.package = 'net.esper.eql.view'
    u.tested_class = 'InternalRouteView'
  end

  a.unit_test('a117') do |u|
    u.package = 'net.esper.eql.view'
    u.tested_class = 'OutputConditionFactoryDefault'
  end

  a.unit_test('a118') do |u|
    u.package = 'net.esper.eql.view'
    u.tested_class = 'OutputProcessViewDirect'
  end

  a.unit_test('a119') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'BeanEventAdapter'
  end

  a.unit_test('a120') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'BeanEventBean'
  end

  a.unit_test('a121') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'BeanEventType'
  end

  a.unit_test('a122') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'CGLibPropertyGetter'
  end

  a.unit_test('a123') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'EventAdapterServiceImpl'
  end

  a.unit_test('a124') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'EventPropertyDescriptor'
  end

  a.unit_test('a125') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'EventPropertyType'
  end

  a.unit_test('a126') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'MapEventBean'
  end

  a.unit_test('a127') do |u|
    u.package = 'net.esper.event'
    u.tested_class = 'MapEventType'
  end

  a.unit_test('a128') do |u|
    u.package = 'net.esper.event.property'
    u.tested_class = 'PropertyListBuilderJavaBean'
  end

  a.unit_test('a129') do |u|
    u.package = 'net.esper.event.property'
    u.tested_class = 'SimpleProperty'
  end

  a.unit_test('a130') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'CombinedEventListener'
  end

  a.unit_test('a131') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'CombinedEventStmt'
  end

  a.unit_test('a132') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'FindMissingEventListener'
  end

  a.unit_test('a133') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'FindMissingEventStmt'
  end

  a.unit_test('a134') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'RealtimeSummaryGroupListener'
  end

  a.unit_test('a135') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'RealtimeSummaryStmt'
  end

  a.unit_test('a136') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'RealtimeSummaryTotalsListener'
  end

  a.unit_test('a137') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'TxnEventA'
  end

  a.unit_test('a138') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'TxnEventB'
  end

  a.unit_test('a139') do |u|
    u.package = 'net.esper.example.transaction'
    u.tested_class = 'TxnEventC'
  end

  a.unit_test('a140') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'FeederOutputStream'
  end

  a.unit_test('a141') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'FieldGenerator'
  end

  a.unit_test('a142') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'PrinterOutputStream'
  end

  a.unit_test('a143') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'ShuffledBucketOutput'
  end

  a.unit_test('a144') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'TransactionEventSource'
  end

  a.unit_test('a145') do |u|
    u.package = 'net.esper.example.transaction.sim'
    u.tested_class = 'TxnGenMain'
  end

  a.unit_test('a146') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'EventTypeIndex'
  end

  a.unit_test('a147') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'EventTypeIndexBuilder'
  end

  a.unit_test('a148') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterHandleSetNode'
  end

  a.unit_test('a149') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterParamExprMap'
  end

  a.unit_test('a150') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterServiceImpl'
  end

  a.unit_test('a151') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterSpecCompiled'
  end

  a.unit_test('a152') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterValueSetImpl'
  end

  a.unit_test('a153') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'FilterValueSetParamComparator'
  end

  a.unit_test('a154') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'IndexTreeBuilder'
  end

  a.unit_test('a155') do |u|
    u.package = 'net.esper.filter'
    u.tested_class = 'IndexTreePath'
  end

  a.unit_test('a156') do |u|
    u.package = 'net.esper.pattern'
    u.tested_class = 'PatternContextFactoryDefault'
  end

  a.unit_test('a157') do |u|
    u.package = 'net.esper.pattern'
    u.tested_class = 'PatternObjectResolutionServiceImpl'
  end

  a.unit_test('a158') do |u|
    u.package = 'net.esper.schedule'
    u.tested_class = 'ScheduleBucket'
  end

  a.unit_test('a159') do |u|
    u.package = 'net.esper.schedule'
    u.tested_class = 'ScheduleSlot'
  end

  a.unit_test('a160') do |u|
    u.package = 'net.esper.schedule'
    u.tested_class = 'SchedulingServiceImpl'
  end

  a.unit_test('a161') do |u|
    u.package = 'net.esper.timer'
    u.tested_class = 'EQLTimerTask'
  end

  a.unit_test('a162') do |u|
    u.package = 'net.esper.timer'
    u.tested_class = 'TimerServiceImpl'
  end

  a.unit_test('a163') do |u|
    u.package = 'net.esper.type'
    u.tested_class = 'MathArithTypeEnum'
  end

  a.unit_test('a164') do |u|
    u.package = 'net.esper.type'
    u.tested_class = 'MinMaxTypeEnum'
  end

  a.unit_test('a165') do |u|
    u.package = 'net.esper.type'
    u.tested_class = 'OuterJoinType'
  end

  a.unit_test('a166') do |u|
    u.package = 'net.esper.util'
    u.tested_class = 'ManagedLockImpl'
  end

  a.unit_test('a167') do |u|
    u.package = 'net.esper.util'
    u.tested_class = 'ManagedReadWriteLock'
  end

  a.unit_test('a168') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'StatementStopServiceImpl'
  end

  a.unit_test('a169') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ViewEnum'
  end

  a.unit_test('a170') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ViewFactoryChain'
  end

  a.unit_test('a171') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ViewFactoryContext'
  end

  a.unit_test('a172') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ViewResolutionServiceImpl'
  end

  a.unit_test('a173') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ViewServiceImpl'
  end

  a.unit_test('a174') do |u|
    u.package = 'net.esper.view'
    u.tested_class = 'ZeroDepthStream'
  end

  a.unit_test('a175') do |u|
    u.package = 'net.esper.view.internal'
    u.tested_class = 'BufferView'
  end

  a.unit_test('a176') do |u|
    u.package = 'net.esper.view.stream'
    u.tested_class = 'StreamFactorySvcImpl'
  end

  a.unit_test('a177') do |u|
    u.package = 'net.esper.view.window'
    u.tested_class = 'TimeWindowView'
  end

  a.unit_test('a178') do |u|
    u.package = 'net.esper.view.window'
    u.tested_class = 'TimeWindowViewFactory'
  end

end

amock_test(:derby) do |a|
  a.system_test = 'org.apache.derby.tools.ij'
  a.args << 'subjects/in/derby/sample.sql'

  # has an array problem, and if that's fixed, has a privacy issue
  a.unit_test('bdb') do |u|
    u.package = 'org.apache.derby.impl.db'
    u.tested_class = 'BasicDatabase'
   end

  a.unit_test('emb') do |u|
    u.package = 'org.apache.derby.jdbc'
    u.tested_class = 'EmbeddedDriver'
  end

  a.unit_test('ec30') do |u|
    u.package = 'org.apache.derby.impl.jdbc'
    u.tested_class = 'EmbedConnection30'
  end

  a.unit_test('ij') do |u|
    u.package = 'org.apache.derby.impl.tools.ij'
    u.tested_class = 'ij'
  end

  a.unit_test('stmt') do |u|
    u.package = 'org.apache.derby.impl.jdbc'
    u.tested_class = 'EmbedStatement'
  end
end

task :drb => [:derby_setup, :derby]
DERBY_OUT_DIR="#{SUBJECTS_OUT}/derby"
directory DERBY_OUT_DIR
task :derby_setup => [DERBY_OUT_DIR] do
  rm_r "#{DERBY_OUT_DIR}/AmockDB" rescue nil
  cp_r 'subjects/in/derby/AmockDB', DERBY_OUT_DIR
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

require 'build/jmodeller_tasks'
