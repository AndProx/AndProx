package au.id.micolous.andprox.di.module;

import android.content.Context;
import android.net.UrlQuerySanitizer;

import javax.inject.Singleton;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.behavior.version.ProxmarkDetection;
import au.id.micolous.andprox.device.ISharedPreferences;
import au.id.micolous.andprox.device.SharedPreferencesImpl;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @Singleton
    public Context provideContext(AndProxApplication application) {
        return application;
    }

    @Provides
    @Singleton
    public ISharedPreferences provideSharedPreferences(Context context) {
        return new SharedPreferencesImpl(context);
    }

    @Provides
    @Singleton
    public ProxmarkDetection provideProxmarkDetection() {
        return new ProxmarkDetection();
    }

}
