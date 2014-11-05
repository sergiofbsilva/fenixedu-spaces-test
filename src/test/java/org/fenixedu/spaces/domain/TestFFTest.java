package org.fenixedu.spaces.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(FenixFrameworkRunner.class)
public class TestFFTest {

    Logger logger = LoggerFactory.getLogger(TestFFTest.class);

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    private void show(String msg, Space space) {
        System.out.println(msg);
        Information info = space.getCurrent();
        while (info != null) {
            System.out.printf("%s ─ %s : %s : %s\n",
                    info.getValidFrom() == null ? "∞" : info.getValidFrom().toString("yyyy-MM-dd"),
                    info.getValidUntil() == null ? "∞" : info.getValidUntil().toString("yyyy-MM-dd"),
                    dateEquals(info.getValidFrom(), info.getValidUntil()), info.getAllocatableCapacity());
            info = info.getPrevious();
        }
    }

    private Boolean dateEquals(DateTime validFrom, DateTime validUntil) {
        if (validFrom == null && validUntil == null) {
            return Boolean.TRUE;
        }
        return validFrom == null ? Boolean.FALSE : validFrom.equals(validUntil);
    }

    @Test
    public void testFirstInsert() {
        DateTime start = new DateTime(2001, 01, 01, 0, 0);
        DateTime end = new DateTime(2015, 01, 01, 0, 0);
        final Information information = createInformation(start, end, 10);
        final Space space = createSpace(information);
        show("after insert at head", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(new DateTime(2001, 01, 01, 0, 0)).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(new DateTime(2012, 01, 01, 0, 0)).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity());
    }

    public void testExpectedException() {
        DateTime start = new DateTime(2001, 01, 01, 0, 0);
        DateTime end = new DateTime(2015, 01, 01, 0, 0);
        final Information information = createInformation(start, end, 10);
        final Space space = createSpace(information);
        Assert.assertTrue(!space.getAllocatableCapacity(new DateTime(2015, 01, 01, 0, 0)).isPresent());
    }

    @Test
    public void testInsertAtHead() {
        DateTime x1 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 12, 31, 0, 0);

        DateTime x3 = new DateTime(2002, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 12, 31, 0, 0);

        final Information information = createInformation(x1, x2, 10);
        final Space space = createSpace(information);
        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("after insert head", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(new DateTime(2001, 05, 01, 0, 0)).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(new DateTime(2002, 05, 01, 0, 0)).get());
    }

    @Test
    public void testInsertAtEnd() {
        DateTime x1 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2002, 01, 01, 0, 0);

        DateTime x3 = new DateTime(2002, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2003, 01, 01, 0, 0);
        DateTime a1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime a2 = new DateTime(2001, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);
        space.add(createInformation(a1, a2, 17));
        show("after insert at tail", space);

        assertEquals("Capacity must be 17", new Integer(17), space.getAllocatableCapacity(a1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());

    }

    @Test
    public void testInsertInTheMiddleDifferentInformations() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);
        Information x = createInformation(x1, x2, 10);
        final Space space = createSpace(x);

        DateTime x3 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 01, 01, 0, 0);
        x = createInformation(x3, x4, 12);
        space.add(x);

        DateTime x5 = new DateTime(2002, 01, 01, 0, 0);
        DateTime x6 = new DateTime(2003, 01, 01, 0, 0);
        Information y = createInformation(x5, x6, 15);

        space.add(y);
        show("after all inserts at head", space);

        DateTime a1 = new DateTime(2000, 05, 01, 0, 0);
        DateTime a2 = new DateTime(2001, 05, 31, 0, 0);
        Information a = createInformation(a1, a2, 20);

        space.add(a);
        show("after insert in the middle", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 20", new Integer(20), space.getAllocatableCapacity(a1.plusDays(3)).get());
        assertEquals("Capacity must be 12", new Integer(12), space.getAllocatableCapacity(a2.plusDays(3)).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x5.plusDays(3)).get());
    }

    @Test
    public void testInsertInTheMiddleSameInformations() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);
        Information x = createInformation(x1, x2, 10);
        final Space space = createSpace(x);

        DateTime x3 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 01, 01, 0, 0);
        x = createInformation(x3, x4, 12);
        space.add(x);

        DateTime x5 = new DateTime(2002, 01, 01, 0, 0);
        DateTime x6 = new DateTime(2015, 01, 01, 0, 0);
        Information y = createInformation(x5, x6, 15);

        space.add(y);
        show("after all inserts at head", space);

        DateTime a1 = new DateTime(2000, 05, 01, 0, 0);
        DateTime a2 = new DateTime(2000, 06, 01, 0, 0);
        Information a = createInformation(a1, a2, 20);

        space.add(a);
        show("after insert in the middle", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 20", new Integer(20), space.getAllocatableCapacity(a1.plusDays(3)).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(a2.plusDays(3)).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x5.plusDays(3)).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity());
    }

    @Test
    public void testInsertTheSamePeriod() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));
        show("first insert", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());

        DateTime x3 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 01, 01, 0, 0);

        space.add(createInformation(x3, x4, 12));
        show("second insert", space);
        assertEquals("Capacity must be 12", new Integer(12), space.getAllocatableCapacity(x3).get());

        space.add(createInformation(x1, x2, 15));
        show("after same insert with different capacity", space);
        assertEquals("Capacity must be 12", new Integer(12), space.getAllocatableCapacity(x3).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x1).get());
    }

    @Test
    public void testOpenEnd() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, null, 10));
        show("first insert", space);
        assertEquals("Capacity is 10", new Integer(10), space.getAllocatableCapacity());

        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x3 = new DateTime(2002, 01, 01, 0, 0);
        space.add(createInformation(x2, x3, 15));
        show("split previous", space);

        assertEquals("Capacity is 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity is 15", new Integer(15), space.getAllocatableCapacity(x2).get());

        DateTime x4 = new DateTime(1990, 01, 01, 0, 0);
        space.add(createInformation(x4, null, 20));
        show("replace all", space);

        assertEquals("Capacity must be 20", new Integer(20), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 20", new Integer(20), space.getAllocatableCapacity(x2).get());
        assertEquals("Capacity must be 20", new Integer(20), space.getAllocatableCapacity(x4).get());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGaps() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        DateTime x3 = new DateTime(2003, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2004, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        Assert.assertTrue("Capacity in the gap must return exception.", !space.getAllocatableCapacity(x2).isPresent());

        space.add(createInformation(x2, x3, 12));

        show("insert in gap", space);
        assertEquals("Capacity is 12", new Integer(12), space.getAllocatableCapacity(x2).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());

        space.add(createInformation(new DateTime(2002, 01, 01, 0, 0), x3, 5));
        show("insert in gap", space);
        assertEquals("Capacity is 12", new Integer(12), space.getAllocatableCapacity(x2).get());
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        assertEquals("Capacity must be 5", new Integer(5), space.getAllocatableCapacity(x3.minusDays(2)).get());

        space.add(createInformation(new DateTime(1990, 01, 01, 0, 0), new DateTime(2020, 01, 01, 0, 0), 100));
        show("after replacement", space);

        assertEquals("Capacity must be 100", new Integer(100), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 100", new Integer(100), space.getAllocatableCapacity(x3).get());
        assertEquals("Capacity must be 100", new Integer(100), space.getAllocatableCapacity(x3.minusDays(2)).get());
    }

    @Test
    public void testGapsInfinity() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = null;

        DateTime x3 = new DateTime(2003, 01, 01, 0, 0);
        DateTime x4 = null;

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        show("get validFrom", space);
        assertEquals("Capacity must be 10", new Integer(15), space.getAllocatableCapacity());

    }

    @Test
    public void testGapsInfinityMiddle() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = null;

        DateTime x3 = new DateTime(2003, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2006, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        show("get validFrom", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity());

    }

    @Test
    public void testGapsInfinityOverlap() {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = null;

        DateTime x3 = new DateTime(1990, 01, 01, 0, 0);
        DateTime x4 = null;

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);
        assertEquals("Capacity must be 10", new Integer(15), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        show("get validFrom", space);
        assertEquals("Capacity must be 10", new Integer(15), space.getAllocatableCapacity());

    }

    @Test
    public void testGapsNotInfinityEnd() {
        DateTime x1 = new DateTime(1998, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        DateTime x3 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x4 = x2;
        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(15), space.getAllocatableCapacity(x3).get());
        show("get validFrom", space);
        assertTrue("no capacity at present date", !space.getInformation().isPresent());

    }

    @Test
    public void testGapsNotInfinityStart() {
        DateTime x1 = new DateTime(1998, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        DateTime x3 = new DateTime(2000, 01, 01, 0, 0);
        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x1, x3, 15));
        show("second insertion", space);
        assertEquals("Capacity must be 10", new Integer(15), space.getAllocatableCapacity(x1).get());
        assertEquals("Capacity must be 15", new Integer(10), space.getAllocatableCapacity(x3).get());
        show("get validFrom", space);
        assertTrue("no capacity at present date", !space.getInformation().isPresent());

    }

    Information createInformation(DateTime x1, DateTime x2, int capacity) {
        return Information.builder().validFrom(x1).validUntil(x2).allocatableCapacity(capacity).build();
    }

    Space createSpace(final Information information) {
        return new Space(information);
    }
}
