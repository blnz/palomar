
package com.blnz.fxpl.xform;

import com.blnz.fxpl.FXContext;


/**
 * allows upstream processing to learn that downstream processor no
 * longer want more input
 */
public class DownstreamProcessStatus
{

    private DownstreamProcessStatus _ancestor = null;
    private boolean _isFinished = false;

    /**
     *
     */
    public DownstreamProcessStatus(FXContext context)
    {
        Object prev = context.get("DownstreamProcessStatus");
        if (prev != null && prev instanceof DownstreamProcessStatus) {
            _ancestor = (DownstreamProcessStatus) prev;
        }
    }

    /**
     *
     */
    public boolean isFinished()
    {
        if (_isFinished) {
            return true;
        } else if (_ancestor != null) {
            return _ancestor.isFinished();
        }
        return false;
    }

    /**
     *
     */
    public void markFinished()
    {
        _isFinished = true;
    }
}
