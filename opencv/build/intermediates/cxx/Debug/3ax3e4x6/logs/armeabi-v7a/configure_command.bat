@echo off
"C:\\Users\\mhentschel\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HC:\\Users\\mhentschel\\FAUbox\\6. Semester\\Projektarbeit\\RunalyzerAPP\\opencv\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=armeabi-v7a" ^
  "-DCMAKE_ANDROID_ARCH_ABI=armeabi-v7a" ^
  "-DANDROID_NDK=C:\\Users\\mhentschel\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\mhentschel\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\mhentschel\\AppData\\Local\\Android\\Sdk\\ndk\\26.1.10909125\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\mhentschel\\AppData\\Local\\Android\\Sdk\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=C:\\Users\\mhentschel\\FAUbox\\6. Semester\\Projektarbeit\\RunalyzerAPP\\opencv\\build\\intermediates\\cxx\\Debug\\3ax3e4x6\\obj\\armeabi-v7a" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=C:\\Users\\mhentschel\\FAUbox\\6. Semester\\Projektarbeit\\RunalyzerAPP\\opencv\\build\\intermediates\\cxx\\Debug\\3ax3e4x6\\obj\\armeabi-v7a" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BC:\\Users\\mhentschel\\FAUbox\\6. Semester\\Projektarbeit\\RunalyzerAPP\\opencv\\.cxx\\Debug\\3ax3e4x6\\armeabi-v7a" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
