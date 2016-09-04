/**
 *
 * Copyright (c) 2016 Dotweblabs Web Technologies and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  __            __       __
 * |  |_.--.--.--|__.-----|  |
 * |   _|  |  |  |  |   --|  |_
 * |____|________|__|___| |____|
 * :: twirl :: Object Mapping ::
 *
 */
package com.google.appengine.api.datastore;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Geo point.
 * GWT emulation
 *
 * @author <a href="mailto:kerbymart@gmail.com">Kerby Martino</a>
 */
@Embeddable
public class GeoPt implements Serializable, Comparable<GeoPt> {
    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private float latitude;
    private float longitude;

    /**
     * Not to be used -- per jpa only.
     */
    @Deprecated
    public GeoPt() {
    }

    public GeoPt(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    @Deprecated
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    @Deprecated
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GeoPt == false)
            return false;

        GeoPt gp = (GeoPt) obj;
        return getLatitude() == gp.getLatitude() && getLongitude() == gp.getLongitude();
    }

    public int hashCode() {
        return (int) (getLatitude() + 7 * getLongitude());
    }

    public java.lang.String toString() {
        return "GeoPt: latitude=" + getLatitude() + ", longitude=" + getLongitude();
    }

    public int compareTo(GeoPt gp) {
        float diff = getLatitude() - gp.getLatitude();
        return (int) ((diff == 0) ? (getLongitude() - gp.getLongitude()) : diff);
    }
}