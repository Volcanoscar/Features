# Use the non-open-source part, if present
-include vendor/malatamobile/mlt6755_65u_l/BoardConfigVendor.mk

# Use the 6752 common part
include device/mediatek/mt6755/BoardConfig.mk

# cqf add MOPLUES-37 20151216
ifeq ($(TARGET_BUILD_VARIANT),user)
WITH_DEXPREOPT := true
endif

#Config partition size
-include $(MTK_PTGEN_OUT)/partition_size.mk
BOARD_CACHEIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_FLASH_BLOCK_SIZE := 4096

include device/malatamobile/$(MTK_TARGET_PROJECT)/ProjectConfig.mk

MTK_INTERNAL_CDEFS := $(foreach t,$(AUTO_ADD_GLOBAL_DEFINE_BY_NAME),$(if $(filter-out no NO none NONE false FALSE,$($(t))),-D$(t))) 
MTK_INTERNAL_CDEFS += $(foreach t,$(AUTO_ADD_GLOBAL_DEFINE_BY_VALUE),$(if $(filter-out no NO none NONE false FALSE,$($(t))),$(foreach v,$(shell echo $($(t)) | tr '[a-z]' '[A-Z]'),-D$(v)))) 
MTK_INTERNAL_CDEFS += $(foreach t,$(AUTO_ADD_GLOBAL_DEFINE_BY_NAME_VALUE),$(if $(filter-out no NO none NONE false FALSE,$($(t))),-D$(t)=\"$($(t))\")) 

COMMON_GLOBAL_CFLAGS += $(MTK_INTERNAL_CDEFS)
COMMON_GLOBAL_CPPFLAGS += $(MTK_INTERNAL_CDEFS)

ifneq ($(MTK_K64_SUPPORT), yes)
BOARD_KERNEL_CMDLINE = bootopt=64S3,32S1,32S1
else
BOARD_KERNEL_CMDLINE = bootopt=64S3,32N2,64N2
endif

ifeq ($(strip $(MTK_IPOH_SUPPORT)), yes)
BOARD_MTK_CACHE_SIZE_KB :=442368
endif

ifeq ($(MTK_EMMC_SUPPORT),yes)
  TARGET_RECOVERY_FSTAB := device/malatamobile/$(MTK_BASE_PROJECT)/recovery_emmc.fstab
else
  ifeq ($(MTK_NAND_UBIFS_SUPPORT),yes)
    TARGET_RECOVERY_FSTAB := device/malatamobile/$(MTK_BASE_PROJECT)/recovery_ubifs.fstab
  else
    TARGET_RECOVERY_FSTAB := device/malatamobile/$(MTK_BASE_PROJECT)/recovery_yaffs2.fstab
  endif
endif

-include device/mediatek/build/build/tools/base_rule_remake.mk
