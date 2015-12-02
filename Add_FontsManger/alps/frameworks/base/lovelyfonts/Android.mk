LOCAL_PATH := $(my-dir)
ifeq ($(strip $(LOVELYFONTS_SUPPORT)),yes)
$(shell chmod 777 $(LOCAL_PATH)/fontd)

$(shell mkdir -p $(PRODUCT_OUT)/system/app/Lovelyfonts)
$(shell mkdir -p $(PRODUCT_OUT)/system/bin)
$(shell mkdir -p $(PRODUCT_OUT)/system/etc)
$(shell mkdir -p $(PRODUCT_OUT)/system/fonts)
$(shell mkdir -p $(PRODUCT_OUT)/system/lib)
$(shell mkdir -p $(PRODUCT_OUT)/system/lib64)
$(shell mkdir -p $(PRODUCT_OUT)/system/fonts/free)

$(shell cp -a $(LOCAL_PATH)/fontd $(PRODUCT_OUT)/system/bin/fontd)

$(shell cp -a $(LOCAL_PATH)/libFonts.so $(PRODUCT_OUT)/system/lib/libFonts.so)
$(shell cp -a $(LOCAL_PATH)/libFonts64.so $(PRODUCT_OUT)/system/lib64/libFonts.so)

$(shell cp -a $(LOCAL_PATH)/Coooie_Apbold-v5.02-8236E43CE3517AEE6252FB4654B1575F.ttf $(PRODUCT_OUT)/system/fonts/free/Coooie_Apbold-v5.02-8236E43CE3517AEE6252FB4654B1575F.ttf)
$(shell cp -a $(LOCAL_PATH)/Coooie_Apis-v5.01-6356C59A0CDFD285BEC55EF0F2108CF6.ttf $(PRODUCT_OUT)/system/fonts/free/Coooie_Apis-v5.01-6356C59A0CDFD285BEC55EF0F2108CF6.ttf)
$(shell cp -a $(LOCAL_PATH)/Coooie_FLYsheep-v5.02-2F951A2EF36160B4798B699264E2D0DB.ttf $(PRODUCT_OUT)/system/fonts/free/Coooie_FLYsheep-v5.02-2F951A2EF36160B4798B699264E2D0DB.ttf)
$(shell cp -a $(LOCAL_PATH)/CryliArabic-v5.02-2C3BA5A6700167F607B794B6F0D9F4FD.ttf $(PRODUCT_OUT)/system/fonts/free/CryliArabic-v5.02-2C3BA5A6700167F607B794B6F0D9F4FD.ttf)
$(shell cp -a $(LOCAL_PATH)/CyrillicOld-v5.01-2B1C93F62B3F79D9C78E4965FAA906E4.ttf $(PRODUCT_OUT)/system/fonts/free/CyrillicOld-v5.01-2B1C93F62B3F79D9C78E4965FAA906E4.ttf)
$(shell cp -a $(LOCAL_PATH)/Mukti_Narrow-v5.01-7BA7B994787F02BDB5CA5C91484E3CD6.ttf $(PRODUCT_OUT)/system/fonts/free/Mukti_Narrow-v5.01-7BA7B994787F02BDB5CA5C91484E3CD6.ttf)
$(shell cp -a $(LOCAL_PATH)/Open_Sans-v5.01-8096EE5D03CBEB7869EE13406A46376A.ttf $(PRODUCT_OUT)/system/fonts/free/Open_Sans-v5.01-8096EE5D03CBEB7869EE13406A46376A.ttf)
$(shell cp -a $(LOCAL_PATH)/Purisa-v5.01-DD3CDD7664EC3449594422C39BD55726.ttf $(PRODUCT_OUT)/system/fonts/free/Purisa-v5.01-DD3CDD7664EC3449594422C39BD55726.ttf)
$(shell cp -a $(LOCAL_PATH)/SagarNormal-v5.01-A68567972E6CEA8110F8318C26554B98.ttf $(PRODUCT_OUT)/system/fonts/free/SagarNormal-v5.01-A68567972E6CEA8110F8318C26554B98.ttf)
$(shell cp -a $(LOCAL_PATH)/Sofadione-v5.02-20227D8A2BF15FEBD1920569773E8DED.ttf $(PRODUCT_OUT)/system/fonts/free/Sofadione-v5.02-20227D8A2BF15FEBD1920569773E8DED.ttf)
$(shell cp -a $(LOCAL_PATH)/Thabit-v5.02-340E43AA01160362B676A54240FABE5B.ttf $(PRODUCT_OUT)/system/fonts/free/Thabit-v5.02-340E43AA01160362B676A54240FABE5B.ttf)
$(shell cp -a $(LOCAL_PATH)/TlwgMono-v5.01-C6B3D03ED9168E207E5993D86254ADAA.ttf $(PRODUCT_OUT)/system/fonts/free/TlwgMono-v5.01-C6B3D03ED9168E207E5993D86254ADAA.ttf)
$(shell cp -a $(LOCAL_PATH)/xingyunfangsong-v1.00-DF083BAEA35641982BB6ECC882741C4A.ttf $(PRODUCT_OUT)/system/fonts/free/xingyunfangsong-v1.00-DF083BAEA35641982BB6ECC882741C4A.ttf)
$(shell cp -a $(LOCAL_PATH)/xingyunshaonian-v1.00-98086EEC234588DE6732CECD8065EB79.ttf $(PRODUCT_OUT)/system/fonts/free/xingyunshaonian-v1.00-98086EEC234588DE6732CECD8065EB79.ttf)
$(shell cp -a $(LOCAL_PATH)/handwrite-v5.11-504B2C0D32A233A1D5077C9493E7FB5D.ttf $(PRODUCT_OUT)/system/fonts/free/handwrite-v5.11-504B2C0D32A233A1D5077C9493E7FB5D.ttf)
$(shell cp -a $(LOCAL_PATH)/Fluttering-v1.67-354733DF1A94060A60D30989BF50FFDC.ttf $(PRODUCT_OUT)/system/fonts/free/Fluttering-v1.67-354733DF1A94060A60D30989BF50FFDC.ttf)
$(shell cp -a $(LOCAL_PATH)/Ink-v1.0-B7A8841C270654786A08A523AB933D07.ttf $(PRODUCT_OUT)/system/fonts/free/Ink-v1.0-B7A8841C270654786A08A523AB933D07.ttf)


ifeq ($(strip $(LOVELYFONTS_ICON_SHOW)),yes)
$(shell cp -a $(LOCAL_PATH)/lovelyfonts_malata_5.0_icon.apk $(PRODUCT_OUT)/system/app/Lovelyfonts/lovelyfonts_malata_5.0_icon.apk)
else
$(shell cp -a $(LOCAL_PATH)/lovelyfonts_malata_5.0_noicon.apk $(PRODUCT_OUT)/system/app/Lovelyfonts/lovelyfonts_malata_5.0_noicon.apk)
endif

include $(call all-subdir-makefiles)
endif

