package com.mattcnew.reliability;

import org.HdrHistogram.IntCountsHistogram;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class LatencyDistribution {

    @Test
    public void meanOfDistributionMayBeAbove99thPercentile() {
        Random random = new Random();

        IntCountsHistogram histogram = new IntCountsHistogram(5);
        for (int i = 0; i < 100; i++) {
            histogram.recordValue(random.nextInt(20));
        }

        histogram.recordValue(10*1000);

        System.out.println("Mean");
        System.out.println(histogram.getMean());
        System.out.println("Percentile 99");
        System.out.println(histogram.getValueAtPercentile(99));

        assertThat(histogram.getMean())
                .isGreaterThan(histogram.getValueAtPercentile(99));


    }
}
