# Manually test Android Keystore

https://issuetracker.google.com/issues/215575432

1. CREATE KEY
2. USE KEY
3. Settings: ADD new fingerprint
4. USE KEY -> this should result in a `KeyPermanentlyInvalidatedException`
5. DELETE KEY
6. CREATE KEY
7. USE KEY
8. Settings: REMOVE fingerprint
9. USE KEY -> this should work! (but doesn't on Pixel phones)
