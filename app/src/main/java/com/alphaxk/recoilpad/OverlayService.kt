trigger.setOnTouchListener { v, e ->
    if (editing) {
        dTrig(v, e)
    } else {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = e.getPointerId(0) // حفظ معرف الإصبع
                isFiring = true
                if (isScoping) {
                    if (Prefs.autoFireEnabled) startAutoFire() else startRecoil(true)
                } else {
                    startRecoil(false)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = e.actionIndex
                val pointerId = e.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    isFiring = false
                    stopAutoFire()
                    RecoilEngine.release()
                }
            }
            MotionEvent.ACTION_UP -> {
                isFiring = false
                activePointerId = -1
                stopAutoFire()
                RecoilEngine.release()
            }
            MotionEvent.ACTION_CANCEL -> {
                // تجاهل
            }
        }
        true
    }
}
