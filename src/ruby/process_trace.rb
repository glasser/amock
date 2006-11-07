require 'rexml/document'

TRACE_FILE = 'trace.xml'

CLASS = "edu.mit.csail.pag.amock.subjects.PositiveIntBox"
ID = 2

processor = nil

def main
  processor = WaitForConstructorToEnd.new

  d = REXML::Document.new(File.new(TRACE_FILE))

  d.each_element("//object[@id=#{ID}]/ancestor-or-self::action") do |e|
    ret = processor.process_action e
    processor = ret if ret
  end
end

class WaitForConstructorToEnd
  def process_action(action)
    if action.attributes['type'] == 'exit' and 
        action.attributes['signature'] =~ /^#{CLASS}\.<init>/
        puts "construct an object with args:"
      puts action.elements['args']
      
      return MonitorCallsOnObject.new
    end
  end
end

class MonitorCallsOnObject
  def process_action(action)
    if action.attributes['type'] == 'enter' and action.elements["receiver[object[@id=#{ID}]]"]
      puts "call a method #{action.attributes['signature']} on it (id #{action.attributes['call']}); args"
      puts action.elements['args']
      return WaitForMethodToEnd.new(action.attributes['call'])
    end
  end
end

class WaitForMethodToEnd
  def initialize(callid)
    @callid = callid
  end
  
  def process_action(action)
    if action == 'exit' and action.attributes['call'] == @callid
      puts "we return:"
      if action.elements['void']
        puts "(void)"
      else
        puts "retval:"
        puts action.elements['return']
      end

      return MonitorCallsOnObject.new
    end
  end
end



main
