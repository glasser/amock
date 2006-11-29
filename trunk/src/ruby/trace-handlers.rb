#!/usr/bin/env ruby

class TraceHandler; end

class WaitForConstructorToEnd < TraceHandler
  def process_action(action, sm)
    if trace_id = action_is_constructor(action, sm.class_generator.classname) and
        trace_id == sm.trace_id

      sm << sm.build_constructor(sm.class_generator.classname, action.args)
      
      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end

class MonitorCallsOnObject < TraceHandler
  def process_action(action, sm)
    if action.type == 'preCall' and 
        action.receiver.object_id == sm.trace_id
      # We'll print out this call when it returns.
      sm.next_state(WaitForMethodToEnd.new(action.call_id))
    end
  end
end

class WaitForMethodToEnd < TraceHandler
  def initialize(callid)
    @callid = callid
  end
  
  def process_action(action, sm)
    if action.type == 'postCall' and action.call_id == @callid
      retval = action.void? ? nil : action.return_value
      sm << sm.build_method_call(action.method_name,
                                 action.args, retval)

      sm.next_state(MonitorCallsOnObject.new)
    end
  end
end
