// This file is part of OpenTSDB.
// Copyright (C) 2012-2018  The OpenTSDB Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package net.opentsdb.data.types.numeric.aggregators;

import com.google.common.base.Strings;
import com.stumbleupon.async.Deferred;

import net.opentsdb.core.BaseTSDBPlugin;
import net.opentsdb.core.TSDB;
import net.opentsdb.data.AggregatorConfig;
import net.opentsdb.data.types.numeric.MutableNumericValue;
import net.opentsdb.exceptions.IllegalDataException;

/**
 * Calculates the product of all values in the array.
 * TODO - handle integer overflows.
 * 
 * @since 3.0
 */
public class MultiplyFactory extends BaseTSDBPlugin implements 
    NumericAggregatorFactory {

  public static final String TYPE = "Multiply";
  
  @Override
  public NumericAggregator newAggregator() {
    return AGGREGATOR;
  }
  
  @Override
  public NumericAggregator newAggregator(final AggregatorConfig config) {
    return AGGREGATOR;
  }
  
  @Override
  public NumericAggregator newAggregator(boolean infectious_nan) {
    return AGGREGATOR;
  }

  @Override
  public String type() {
    return TYPE;
  }
  
  @Override
  public Deferred<Object> initialize(final TSDB tsdb, final String id) {
    this.id = Strings.isNullOrEmpty(id) ? TYPE : id;
    return Deferred.fromResult(null);
  }

  @Override
  public String version() {
    return "3.0.0";
  }

  private static final class Multiply extends BaseNumericAggregator {
    public Multiply(final String name) {
      super(name);
    }

    @Override
    public void run(final long[] values, 
                    final int start_offset,
                    final int end_offset, 
                    final MutableNumericValue dp) {
      if (end_offset < 1) {
        throw new IllegalDataException("End offset must be greater than 0");
      }
      
      // TODO - overflow
      long product = 1;
      for (int i = start_offset; i < end_offset; i++) {
        product *= values[i];
      }
      dp.resetValue(product);
    }
    
    @Override
    public void run(final double[] values, 
                    final int start_offset,
                    final int end_offset, 
                    final boolean infectious_nans,
                    final MutableNumericValue dp) {
      if (end_offset < 1) {
        throw new IllegalDataException("End offset must be greater than 0");
      }
      
      double product = 1;
      int nans = 0;
      for (int i = start_offset; i < end_offset; i++) {
        if (Double.isNaN(values[i])) {
          nans++;
          continue;
        }
        product *= values[i];
      }
      if (nans == end_offset - start_offset || (nans > 0 && infectious_nans)) {
        dp.resetValue(Double.NaN);
      } else {
        dp.resetValue(product);
      }
    }
    
  }
  private static final NumericAggregator AGGREGATOR = new Multiply(TYPE);
}
