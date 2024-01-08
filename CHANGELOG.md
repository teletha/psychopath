# Changelog

## [1.12.2](https://github.com/teletha/psychopath/compare/v1.12.1...v1.12.2) (2024-01-08)


### Bug Fixes

* update ci process ([37ad45b](https://github.com/teletha/psychopath/commit/37ad45b41da95fe2b5f428f429c29f8032f7152f))
* update license ([da720de](https://github.com/teletha/psychopath/commit/da720def29bbc22d1812c58ebb536775abc8c4b6))
* Use independent thread pool for observing modification. ([ca0d343](https://github.com/teletha/psychopath/commit/ca0d343e672b9e9e9a4fc7fbdfe298d60ee28fe3))

## [1.12.1](https://github.com/teletha/psychopath/compare/v1.12.0...v1.12.1) (2023-06-06)


### Bug Fixes

* Update sinobu. ([fa295ef](https://github.com/teletha/psychopath/commit/fa295ef6b9c0baed433e38a3b02a6de8ea538b52))

## [1.12.0](https://github.com/teletha/psychopath/compare/v1.11.0...v1.12.0) (2023-05-24)


### Features

* Add Location#isBefore and #isAfter. ([9d34148](https://github.com/teletha/psychopath/commit/9d3414853313627f6ce16d020ff0893f9cbcf6e1))

## [1.11.0](https://github.com/teletha/psychopath/compare/v1.10.0...v1.11.0) (2023-05-24)


### Features

* Add File#asArchiveDirectory. ([b084a3d](https://github.com/teletha/psychopath/commit/b084a3ded1c9684a00bcc6f1a8d057596f3ab0ed))
* Add sync option. ([d45c8a6](https://github.com/teletha/psychopath/commit/d45c8a6e54ea0b03a0ddd86556db7ff1b22d154d))


### Bug Fixes

* NPE ([b8f605a](https://github.com/teletha/psychopath/commit/b8f605aa784b4a03faa6928e558b80894e15e310))

## [1.10.0](https://github.com/teletha/psychopath/compare/v1.9.0...v1.10.0) (2023-05-20)


### Features

* Add Option#replaceDifferent. ([04a82bf](https://github.com/teletha/psychopath/commit/04a82bf48f8238e903379001eddb715e5ca27a42))

## [1.9.0](https://github.com/teletha/psychopath/compare/v1.8.0...v1.9.0) (2023-05-19)


### Features

* Implements Option#skipExisting and Option#replaceOld. ([5768587](https://github.com/teletha/psychopath/commit/5768587a271fe36232a2922b20413f34ab823766))

## [1.8.0](https://github.com/teletha/psychopath/compare/v1.7.1...v1.8.0) (2023-05-15)


### Features

* Add trackable unpacking method on File. ([b7c97ba](https://github.com/teletha/psychopath/commit/b7c97ba74273214b146926c43e05f07592be2d01))
* Location is serializable. ([86ad59c](https://github.com/teletha/psychopath/commit/86ad59ccbeb4254651c156caaafafe4a131b215f))


### Bug Fixes

* Progress for archive is broken. ([b7e78c0](https://github.com/teletha/psychopath/commit/b7e78c0eed8add23be3dbf97c27793ec9c85a56a))

## [1.7.1](https://github.com/teletha/psychopath/compare/v1.7.0...v1.7.1) (2023-01-02)


### Bug Fixes

* test ([9af0b68](https://github.com/teletha/psychopath/commit/9af0b688d1c13188d4157842eedc6d9dd610f9a1))

## [1.7.0](https://github.com/teletha/psychopath/compare/v1.6.0...v1.7.0) (2023-01-01)


### Features

* Remove Location#lock. ([3dee183](https://github.com/teletha/psychopath/commit/3dee1835f9f2370cc7a33347e7a7cf20a4ac34b0))


### Bug Fixes

* Atomic writing mode generates backup file. ([32c285e](https://github.com/teletha/psychopath/commit/32c285e4d0056c576de07083787b3d8d009a0478))
* PathOperatable must be public. ([1f4123b](https://github.com/teletha/psychopath/commit/1f4123b529311b88239a75891eccf618d731d9ba))
* Update java to 17. ([8e1793c](https://github.com/teletha/psychopath/commit/8e1793c660b33d276037dd6fc2c6d4a291ed3009))

## [1.6.0](https://www.github.com/teletha/psychopath/compare/v1.5.0...v1.6.0) (2021-12-16)


### Features

* Add File#text(Function) to replace text contents. ([ebdb01f](https://www.github.com/teletha/psychopath/commit/ebdb01f2a9294673e055ddb4305f70db2020d5a9))

## [1.5.0](https://www.github.com/teletha/psychopath/compare/v1.4.2...v1.5.0) (2021-12-08)


### Features

* Add PathOperatable#existFile. ([c4ff5cc](https://www.github.com/teletha/psychopath/commit/c4ff5ccc9966cb02f1a05879169d28ef7a3a6fbf))

### [1.4.2](https://www.github.com/teletha/psychopath/compare/v1.4.1...v1.4.2) (2021-12-04)


### Bug Fixes

* Revert Location#create. ([50705c0](https://www.github.com/teletha/psychopath/commit/50705c0a507b733fe670d36cf9e5e2240ec4eaa0))

### [1.4.1](https://www.github.com/teletha/psychopath/compare/v1.4.0...v1.4.1) (2021-12-04)


### Bug Fixes

* Drop file permissions. ([c179256](https://www.github.com/teletha/psychopath/commit/c179256d210b7cea971d31bcb776867f001d7c7e))

## [1.4.0](https://www.github.com/teletha/psychopath/compare/v1.3.1...v1.4.0) (2021-12-04)


### Features

* Location#create accepts FileAttribute. ([5de2f25](https://www.github.com/teletha/psychopath/commit/5de2f2550dfa9f40db70581e79efb97635b34ec7))
* Safe temporary system. ([8966042](https://www.github.com/teletha/psychopath/commit/8966042cdb4120150d22c38dba149b9bd629b3c2))


### Bug Fixes

* temporary root ([c144992](https://www.github.com/teletha/psychopath/commit/c144992ead5b020066135eff5183180116703d00))

### [1.3.1](https://www.github.com/teletha/psychopath/compare/v1.3.0...v1.3.1) (2021-12-03)


### Bug Fixes

* Ensure the existence of temporary root directory. ([91ac14c](https://www.github.com/teletha/psychopath/commit/91ac14c0248ca81adc68c9529732de59f140f959))

## [1.3.0](https://www.github.com/Teletha/psychopath/compare/v1.2.1...v1.3.0) (2021-10-23)


### Features

* Add PathOperatable#trackXXX methods with detailed progress info. ([1476ffb](https://www.github.com/Teletha/psychopath/commit/1476ffbfd1da169e8114f8b8b8bf42ce903fe709))


### Bug Fixes

* All file operations should send the own event before its execution. ([87e11b1](https://www.github.com/Teletha/psychopath/commit/87e11b184a3e809bbaaa39b50c39979e9a597ee2))

### [1.2.1](https://www.github.com/Teletha/psychopath/compare/v1.2.0...v1.2.1) (2021-03-25)


### Bug Fixes

* Ext file system doesn't support creation time. ([307d515](https://www.github.com/Teletha/psychopath/commit/307d515a4cce2cc7dfbd4235c4516df74e9bf658))
* Some archiver depends on the specifiec platform. ([e0a0a01](https://www.github.com/Teletha/psychopath/commit/e0a0a0162064ed12e6f9d9fd40ef28d1109167f1))

## [1.2.0](https://www.github.com/Teletha/psychopath/compare/v1.1.0...v1.2.0) (2021-03-24)


### Features

* Use Option.ATOMIC_WRITE instead of PsychopathOpenOption. ([fafd149](https://www.github.com/Teletha/psychopath/commit/fafd149e5ea2be7c394920525a84924c66c9c1a4))

## 1.1.0 (2021-03-23)


### Bug Fixes

* File#text throws error when file is absent. ([fc36377](https://www.github.com/Teletha/psychopath/commit/fc363779adc7344b34a707889b499e0b56463bb1))
* Folder packing. ([dae75ba](https://www.github.com/Teletha/psychopath/commit/dae75ba1ba8ffd057d4ac234493be0f1302ce98b))
