#!/usr/bin/env ruby

class TraceAction
  def initialize(xml)
    @xml = xml
  end

  def type
    @xml.name
  end
  
  def method_name
    @xml.attributes['name']
  end
  
  def owner
    @xml.attributes['owner']
  end

  def owner_external
    internal_to_external_classname(owner)
  end

  def internal_to_external_classname(classname)
    classname.gsub '/', '.'
  end

  def receiver
    TraceItem.new(@xml.elements['receiver'].elements[1])
  end

  def has_receiver?
    @xml.elements['receiver'] ? true : false
  end

  def return_value
    TraceItem.new(@xml.elements['return'].elements[1])
  end

  def args
    @xml.elements['args'].collect do |arg|
      TraceItem.new(arg)
    end
  end

  def call_id
    @xml.attributes['call']
  end

  def void?
    @xml.elements['void'] != nil
  end
end

class TraceItem
  def initialize(xml)
    @xml = xml
  end
  
  def object_id
    raise unless @xml.name == "object"
    @xml.attributes['id']
  end

  def type
    @xml.name
  end

  def primitive_value
    raise unless @xml.name == "primitive"
    @xml.attributes["value"]
  end

  def unsafe_text # XXX refactor, escaping, etc
    @xml.text
  end

  def classname
    raise unless @xml.name == "object"
    @xml.attributes['class']
  end

end
