gcc -arch i386 -arch x86_64 -mmacosx-version-min=10.5 -framework ApplicationServices -I/System/Library/Frameworks/JavaVM.framework/Headers/ cursorHider.c -shared -fPIC -o cursorHider.jnilib