package au.id.micolous.andprox.di.component;

import android.app.Application;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.di.DaggerApplication;
import au.id.micolous.andprox.di.module.AppModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;

@Component(modules = {AppModule.class})
public interface AppComponent extends AndroidInjector<DaggerApplication> {

    void inject(Application application);

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(DaggerApplication application);
        AppComponent build();

    }
}
