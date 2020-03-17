/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.persistence.rest;

import io.arlas.persistence.model.DataResource;
import io.arlas.persistence.model.DataWithLinks;
import io.arlas.persistence.model.Link;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.SortOrder;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataHALService {

    public DataResource dataListToResource(Pair<Long, List<Data>> dataList, UriInfo uriInfo, Integer page, Integer size, SortOrder order) {
        DataResource dataResource = new DataResource();
        dataResource.total = dataList.getLeft();
        dataResource.count = dataList.getRight().size();
        dataResource.data = dataList.getRight().stream().map(d -> new DataWithLinks(d)).collect(Collectors.toList());
        dataResource.data.replaceAll(u -> dataWithLinks(u, uriInfo));
        dataResource.links = pageLinks(uriInfo, page, size, dataResource.total, dataResource.data.size());
        return dataResource;
    }

    public DataWithLinks dataWithLinks(Data data, UriInfo uriInfo) {
        String subUri = uriInfo.getRequestUriBuilder()
                .path(data.getId())
                .replaceQueryParam("page", null)
                .replaceQueryParam("size", null)
                .replaceQueryParam("type", null)
                .replaceQueryParam("order", null)
                .build().toString();
        Map<String, Link> links = new HashMap<>();
        links.put("self", new Link("self", subUri, "GET"));
        links.put("update", new Link("update", subUri, "PUT"));
        links.put("delete", new Link("delete", subUri, "DELETE"));
        return new DataWithLinks(data).withLinks(links);
    }

    private Map<String, Link> pageLinks(UriInfo uriInfo, Integer page, Integer size, Long total, Integer count) {
        UriBuilder uri = uriInfo.getRequestUriBuilder();
        Map<String, Link> links = new HashMap<>();
        links.put("self", new Link("self", getUri(uri, size, page), "GET"));
        if (page != 1)
            links.put("first", new Link("first", getUri(uri, size, 1), "GET"));
        if (page > 1)
            links.put("prev", new Link("prev", getUri(uri, size, page-1), "GET"));
        if ((page-1)*size + count < total)
            links.put("next", new Link("next", getUri(uri, size, page+1), "GET"));
        if ((page-1)*size + count != total)
            links.put("last", new Link("last", getUri(uri, size, new Double(Math.ceil((double)total/(double)size)).intValue()), "GET"));
        return links;
    }

    private String getUri(UriBuilder uri, Integer size, Integer page) {
        return uri.replaceQueryParam("size", size).replaceQueryParam("page", page).build().toString();
    }

}
