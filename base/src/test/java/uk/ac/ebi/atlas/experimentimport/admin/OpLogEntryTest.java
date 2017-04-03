package uk.ac.ebi.atlas.experimentimport.admin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OpLogEntryTest {

    @Test
    public void testToAndFromArray(){
        for(Op op: Op.values()){
            toArrayAndFromArrayAreOpposites(OpLogEntry.newlyStartedOp(op));
            toArrayAndFromArrayAreOpposites(OpLogEntry.newlyStartedOp(op).toFailure(""));
            toArrayAndFromArrayAreOpposites(OpLogEntry.newlyStartedOp(op).toFailure(new NullPointerException().toString()));
            toArrayAndFromArrayAreOpposites(OpLogEntry.newlyStartedOp(op).toSuccess());

        }
    }

    @Test
    public void testInProgress(){
        for(Op op: Op.values()){
            assertThat(OpLogEntry.newlyStartedOp(op).isInProgress(), is(true));
            assertThat(OpLogEntry.newlyStartedOp(op).toFailure("").isInProgress(), is(false));
            assertThat(OpLogEntry.newlyStartedOp(op).toSuccess().isInProgress(), is(false));
        }
    }

    @Test
    public void testStatus(){
        long start = new Random().nextLong();
        int duration = new Random().nextInt(10000);
        assertThat(OpLogEntry.successfulOp(Op.UPDATE_PUBLIC, start, start+duration).statusMessage(),
                containsString(duration/1000 +" seconds"));
    }


    void toArrayAndFromArrayAreOpposites(OpLogEntry opLogEntry){
        assertThat(OpLogEntry.fromArray(opLogEntry.toArray()), is(opLogEntry));

    }
}