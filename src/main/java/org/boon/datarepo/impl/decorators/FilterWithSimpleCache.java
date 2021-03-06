/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package org.boon.datarepo.impl.decorators;

import org.boon.cache.Cache;
import org.boon.cache.CacheType;
import org.boon.cache.SimpleConcurrentCache;
import org.boon.criteria.ObjectFilter;
import org.boon.criteria.internal.Criteria;
import org.boon.criteria.internal.Group;
import org.boon.datarepo.Filter;
import org.boon.datarepo.ResultSet;

public class FilterWithSimpleCache extends FilterDecoratorBase {

    /* The fifo cache is meant for a routine that is maybe using a few queries in a loop. */
    private Cache<Criteria, ResultSet> fifoCache = new SimpleConcurrentCache<Criteria, ResultSet>( 50, false, CacheType.FIFO );
    private Cache<Criteria, ResultSet> lruCache = new SimpleConcurrentCache<Criteria, ResultSet>( 1000, false, CacheType.LRU );


    @Override
    public ResultSet filter( Criteria... expressions ) {
        Group and = ObjectFilter.and( expressions );

        ResultSet results = fifoCache.get( and );


        if ( results == null ) {
            results = lruCache.get( and );
            if ( results != null ) {
                fifoCache.put( and, results );
                return results;
            }
        }


        results = super.filter( expressions );

        fifoCache.put( and, results );
        lruCache.put( and, results );

        return results;
    }

    @Override
    public void invalidate() {

          /* The fifo cache is meant for a routine that is maybe using a few queries in a loop. */
        fifoCache = new SimpleConcurrentCache<Criteria, ResultSet>( 50, false, CacheType.FIFO );
        lruCache = new SimpleConcurrentCache<Criteria, ResultSet>( 1000, false, CacheType.LRU );
        super.invalidate();
    }

    public FilterWithSimpleCache( Filter delegate ) {
        super( delegate );
    }


}
