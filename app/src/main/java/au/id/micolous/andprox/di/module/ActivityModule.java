package au.id.micolous.andprox.di.module;

import au.id.micolous.andprox.activities.MainActivity;
import au.id.micolous.andprox.di.scope.ProxmarkScope;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {

    @ProxmarkScope
    @ContributesAndroidInjector(modules = MainModule.class)
    abstract MainActivity contributeMainActivity();

}
