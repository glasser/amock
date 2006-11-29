#!/usr/bin/env ruby

class TraceHandler; end

class WaitForConstructorToEnd < TraceHandler
  def process_action(action, sm)
    if trace_id = action_is_constructor(action, sm.generator.classname) and
        trace_id == sm.trace_id

      sm << sm.build_constructor(sm.generator.classname, action.elements['args'])
      
      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

class MonitorCallsOnObject < TraceHandler
  def process_action(action, sm)
    if action.name == 'preCall' and 
        action.elements["receiver[object[@id=#{sm.trace_id}]]"]
      # We'll print out this call when it returns.
      sm.next_state(WaitForMethodToEnd.new(action.attributes['call']))
    end
  end
end

class WaitForMethodToEnd < TraceHandler
  def initialize(callid)
    @callid = callid
  end
  
  def process_action(action, sm)
    if action.name == 'postCall' and action.attributes['call'] == @callid
      retval = action.elements['void'] ? nil : action.elements['return'].elements[1]
      sm << sm.build_method_call(action.attributes['name'],
                                 action.elements['args'], retval)

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end
