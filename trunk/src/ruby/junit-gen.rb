#!/usr/bin/env ruby

class TestClassGenerator
  attr_reader :classname, :processors, :imports
  
  def initialize(classname)
    @classname = classname
    @processors = []
    @imports = {}
  end
  
  def process_action(action)
    if trace_id = action_is_constructor(action, classname)
      processors << TestMethodGenerator.new(trace_id, self)
    end

    processors.each {|p| p.process_action(action)}
  end

  def print_file
    print_file_header
    processors.each {|p| p.print_method}
    print_file_footer
  end

  def print_file_header
    puts "package edu.mit.csail.pag.amock.subjects.generated;"
    puts
    puts "import org.jmock.cglib.MockObjectTestCase;"
    puts "import org.jmock.Mock;"
    imports.each_value do |full_name|
      puts "import #{full_name};"
    end
    puts
    puts "public class GeneratedTests extends MockObjectTestCase {"
  end

  def print_file_footer
    puts "}"
  end

  def get_classname(classname)
    unless m = classname.match(/\.(\w+)$/)
      raise "Weird class name: #{classname}"
    end
    
    shortname = m[1]
    if imports[shortname] and imports[shortname] != classname
      return classname
    else
      imports[shortname] = classname
      return shortname
    end
  end
end

class TestMethodGenerator
  attr_reader :class_generator, :trace_id, :lines, :other_objects
  attr_accessor :handler

  def initialize(trace_id, class_generator)
    @trace_id = trace_id
    @class_generator = class_generator
    @lines = []
    @other_objects = {}
    self.handler = WaitForConstructorToEnd.new
  end

  def process_action(action)
    handler.process_action(action, self)
  end

  def next_state(s)
    self.handler = s
  end
 
  def <<(line)
    lines << line
  end

  def print_method
    print_method_header
    lines.each {|l| puts "        #{l}" }
    print_method_footer
  end

  def print_method_header
    puts "    public void test#{trace_id}() {"
  end

  def print_method_footer
    puts "    }"
    puts
  end

  def build_constructor(classname, args)
    classname = class_generator.get_classname(classname)
    return "#{classname} testedObject = new #{classname}(" +
      args.collect {|a| javafy_item(a)}.join(', ') +
      ");"
  end
  
  def build_method_call(name, args, retval)
    line = ""
    line += "assertEquals(#{javafy_item(retval)}, " if retval
    line += "testedObject.#{name}("
    line += args.elements.collect {|a| javafy_item(a)}.join(', ')
    line += ")"
    line += ")" if retval
    line += ";"
    return line
  end

  def javafy_item(item)
    case item.type
    when "primitive"
      # XXX: Special case character
      return item.primitive_value
    when "null"
      return "null"
    when "string"
      return item.unsafe_text # XXX escaping!
    when "object"
      return javafy_reference(item)
    end
  end

  def javafy_reference(item)
    other_class = class_generator.get_classname(item.class_name)
    other_id = item.object_id
    
    # TODO: better names
    mock_name = "mock#{other_id}"
    proxy_name = "proxy#{other_id}"
    unless other_objects[mock_name]
      # XXX need to make a mock
      lines << "Mock #{mock_name} = mock(#{other_class}.class, new Class[] { int.class }, new Object[] { 24 });"
      lines << "#{other_class} #{proxy_name} = (#{other_class}) #{mock_name}.proxy();"
      other_objects[mock_name] = true
    end
    return proxy_name
  end
end
