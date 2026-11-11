import { useEffect, useRef, useState } from 'react';

export default function HelpHeading({ title, children }) {
  const [open, setOpen] = useState(false);
  const root = useRef(null);

  useEffect(() => {
    const closeOutside = (event) => {
      if (root.current && !root.current.contains(event.target)) setOpen(false);
    };
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') setOpen(false);
    };
    document.addEventListener('click', closeOutside);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('click', closeOutside);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, []);

  return (
    <h2 ref={root} className="help-heading">
      <button type="button" className="help-toggle" aria-expanded={open} onClick={() => setOpen((value) => !value)}>
        {title}<span className="help-mark" aria-hidden="true">?</span>
      </button>
      {open && <div className="bubble" role="tooltip">{children}</div>}
    </h2>
  );
}
