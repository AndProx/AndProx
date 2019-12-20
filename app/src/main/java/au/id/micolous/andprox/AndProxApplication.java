/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2019 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */

package au.id.micolous.andprox;


import android.app.Application;

import au.id.micolous.andprox.activities.AppCompatPreferenceActivity;
import au.id.micolous.andprox.activities.InjectableActivity;
import au.id.micolous.andprox.components.InjectableFragment;
import au.id.micolous.andprox.di.component.AppComponent;
import au.id.micolous.andprox.di.component.DaggerAppComponent;
import au.id.micolous.andprox.di.module.AppModule;

/**
 * AndProx application reference.
 */
public class AndProxApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public void inject(InjectableFragment fragment) {
        appComponent.inject(fragment);
    }

    public void inject(InjectableActivity activity) {
        appComponent.inject(activity);
    }

    public void inject(AppCompatPreferenceActivity activity) {
        appComponent.inject(activity);
    }
}
