package au.id.micolous.andprox.di.component;

import android.app.Activity;
import android.support.v4.app.Fragment;

import javax.inject.Singleton;

import au.id.micolous.andprox.di.DaggerApplication;
import au.id.micolous.andprox.di.module.ActivityModule;
import au.id.micolous.andprox.di.module.AppModule;
import au.id.micolous.andprox.di.module.MainModule;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ActivityModule.class,
        MainModule.class,
        AppModule.class
})
public interface AppComponent extends AndroidInjector<DaggerApplication> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<DaggerApplication> { }
}
