# ThirdSdkLib

### Step 1. Add the JitPack repository to your build file
```
...
allprojects {
    repositories {
        ...
        maven(){url 'https://jitpack.io'}
    }
}
...
```
### Step 2. Add the dependency
```
	dependencies {
	        implementation 'com.github.obelieve:ThirdSdkLib:1.0.0'
	}
```

### Step 3. Use
```
For Utils：
alipay
wechat pay
qiniu upload
```