package edu.mit.csail.pag.amock.tests;

import edu.mit.csail.pag.amock.trace.Hierarchy;
import edu.mit.csail.pag.amock.trace.HierarchyEntry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.*;

public class HierarchyTests extends AmockUnitTestCase {
    private final Set<HierarchyEntry> hes = new HashSet<HierarchyEntry>();

    private void he(String cls, String supe, String... ifs) {
        hes.add(new HierarchyEntry(cls, supe, ifs, true));
    }
    
    {
        he("Grandkid", "Kid");
        he("Kid", "Gramps", "I1");
        he("Gramps", "java/lang/Object", "I2");
        he("Uncle", "Gramps", "I1");
        he("I1", "java/lang/Object", "I2");
        he("I2", "java/lang/Object");
    }
    
    private final Hierarchy hierarchy = new Hierarchy(hes);


    private String mgc(String base, String... must) {
        return hierarchy.getMostGeneralClass(base, Arrays.asList(must));
    }
    
    public void testUnknown() {
        assertThat(mgc("Stranger"), is("Stranger"));
    }

    public void testUnknownImplemented() {
        assertThat(mgc("Uncle", "Wacky"), is("Uncle"));
    }

    public void testIrrelevantImplemented() {
        assertThat(mgc("Uncle", "Grandkid"), is("Uncle"));
    }

    public void testImplementedContainsSelf() {
        assertThat(mgc("Kid", "I2", "Kid", "Gramps"), is("Kid"));
    }

    public void testDontNeedToBeAnything() {
        assertThat(mgc("Kid"), is("java/lang/Object"));
    }

    public void testJustOneAncestor() {
        assertThat(mgc("Grandkid", "I2"), is("I2"));
    }

    public void testAFewAncestors() {
        assertThat(mgc("Grandkid", "I1", "Gramps"), is("Kid"));
    }

    public void testAFewAncestorsOrderless() {
        assertThat(mgc("Grandkid", "Gramps", "I1"), is("Kid"));
    }

    public void testParentInterfaces() {
        assertThat(mgc("Grandkid", "I1", "I2", "java/lang/Object"), is("I1"));
    }

}
