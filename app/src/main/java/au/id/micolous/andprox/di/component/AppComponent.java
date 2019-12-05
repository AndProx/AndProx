package au.id.micolous.andprox.di.component;

import android.app.Activity;
import android.support.v4.app.Fragment;

import au.id.micolous.andprox.di.DaggerApplication;
import au.id.micolous.andprox.di.module.ActivityModule;
import au.id.micolous.andprox.di.module.AppModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;

@Component(modules = {ActivityModule.class, AppModule.class})
public interface AppComponent extends AndroidInjector<DaggerApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(DaggerApplication application);
        AppComponent build();

    }
}
