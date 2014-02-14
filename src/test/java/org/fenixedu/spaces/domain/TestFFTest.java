package org.fenixedu.spaces.domain;

import static org.junit.Assert.assertEquals;

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
            System.out.printf("%s %s %s\n", info.getValidFrom().toString("yyyy-MM-dd"), info.getValidUntil() == null ? "-" : info
                    .getValidUntil().toString("yyyy-MM-dd"), info.getAllocatableCapacity());
            info = info.getPrevious();
        }
    }

    @Test
    public void testFirstInsert() throws UnavailableException {
        DateTime start = new DateTime(2001, 01, 01, 0, 0);
        DateTime end = new DateTime(2015, 01, 01, 0, 0);
        final Information information = createInformation(start, end, 10);
        final Space space = createSpace(information);
        show("after insert at head", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(new DateTime(2001, 01, 01, 0, 0)));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(new DateTime(2012, 01, 01, 0, 0)));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity());
    }

    @Test(expected = UnavailableException.class)
    public void testExpectedException() throws UnavailableException {
        DateTime start = new DateTime(2001, 01, 01, 0, 0);
        DateTime end = new DateTime(2015, 01, 01, 0, 0);
        final Information information = createInformation(start, end, 10);
        final Space space = createSpace(information);
        space.getCapacity(new DateTime(2015, 01, 01, 0, 0));
    }

    @Test
    public void testInsertAtHead() throws UnavailableException {
        DateTime x1 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 12, 31, 0, 0);

        DateTime x3 = new DateTime(2002, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 12, 31, 0, 0);

        final Information information = createInformation(x1, x2, 10);
        final Space space = createSpace(information);
        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("after insert head", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(new DateTime(2001, 05, 01, 0, 0)));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(new DateTime(2002, 05, 01, 0, 0)));
    }

    @Test
    public void testInsertAtEnd() throws UnavailableException {
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

        assertEquals("Capacity must be 17", new Integer(17), space.getCapacity(a1));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x3));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));

    }

    @Test
    public void testInsertInTheMiddleDifferentInformations() throws UnavailableException {
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

        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity must be 20", new Integer(20), space.getCapacity(a1.plusDays(3)));
        assertEquals("Capacity must be 12", new Integer(12), space.getCapacity(a2.plusDays(3)));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x5.plusDays(3)));
    }

    @Test
    public void testInsertInTheMiddleSameInformations() throws UnavailableException {
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

        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity must be 20", new Integer(20), space.getCapacity(a1.plusDays(3)));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(a2.plusDays(3)));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x5.plusDays(3)));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity());
    }

    @Test
    public void testInsertTheSamePeriod() throws UnavailableException {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));
        show("first insert", space);
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));

        DateTime x3 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2002, 01, 01, 0, 0);

        space.add(createInformation(x3, x4, 12));
        show("second insert", space);
        assertEquals("Capacity must be 12", new Integer(12), space.getCapacity(x3));

        space.add(createInformation(x1, x2, 15));
        show("after same insert with different capacity", space);
        assertEquals("Capacity must be 12", new Integer(12), space.getCapacity(x3));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x1));
    }

    @Test
    public void testOpenEnd() throws UnavailableException {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, null, 10));
        show("first insert", space);
        assertEquals("Capacity is 10", new Integer(10), space.getCapacity());

        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);
        DateTime x3 = new DateTime(2002, 01, 01, 0, 0);
        space.add(createInformation(x2, x3, 15));
        show("split previous", space);

        assertEquals("Capacity is 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity is 15", new Integer(15), space.getCapacity(x2));

        DateTime x4 = new DateTime(1990, 01, 01, 0, 0);
        space.add(createInformation(x4, null, 20));
        show("replace all", space);

        assertEquals("Capacity must be 20", new Integer(20), space.getCapacity(x1));
        assertEquals("Capacity must be 20", new Integer(20), space.getCapacity(x2));
        assertEquals("Capacity must be 20", new Integer(20), space.getCapacity(x4));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGaps() throws UnavailableException {
        DateTime x1 = new DateTime(2000, 01, 01, 0, 0);
        DateTime x2 = new DateTime(2001, 01, 01, 0, 0);

        DateTime x3 = new DateTime(2003, 01, 01, 0, 0);
        DateTime x4 = new DateTime(2004, 01, 01, 0, 0);

        final Space space = createSpace(createInformation(x1, x2, 10));

        show("first insertion", space);
        space.add(createInformation(x3, x4, 15));
        show("second insertion", space);

        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x3));
        try {
            space.getCapacity(x2);
            Assert.fail("Capacity in the gap must return exception.");
        } catch (UnavailableException e) {
        }
        space.add(createInformation(x2, x3, 12));
        show("insert in gap", space);
        assertEquals("Capacity is 12", new Integer(12), space.getCapacity(x2));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x3));

        space.add(createInformation(new DateTime(2002, 01, 01, 0, 0), x3, 5));
        show("insert in gap", space);
        assertEquals("Capacity is 12", new Integer(12), space.getCapacity(x2));
        assertEquals("Capacity must be 10", new Integer(10), space.getCapacity(x1));
        assertEquals("Capacity must be 15", new Integer(15), space.getCapacity(x3));
        assertEquals("Capacity must be 5", new Integer(5), space.getCapacity(x3.minusDays(2)));

        space.add(createInformation(new DateTime(1990, 01, 01, 0, 0), new DateTime(2020, 01, 01, 0, 0), 100));
        show("after replacement", space);

        assertEquals("Capacity must be 100", new Integer(100), space.getCapacity(x1));
        assertEquals("Capacity must be 100", new Integer(100), space.getCapacity(x3));
        assertEquals("Capacity must be 100", new Integer(100), space.getCapacity(x3.minusDays(2)));
    }

    Information createInformation(DateTime x1, DateTime x2, int capacity) {
        return Information.builder().validFrom(x1).validUntil(x2).allocatableCapacity(capacity).build();
    }

    Space createSpace(final Information information) {
        return new Space(information);
    }
}
