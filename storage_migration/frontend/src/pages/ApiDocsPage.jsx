import { useEffect, useRef, useState } from 'react';
import SwaggerUIBundle from 'swagger-ui-dist/swagger-ui-bundle.js';
import 'swagger-ui-dist/swagger-ui.css';
import { getWorker } from '../api.js';
import HelpHeading from '../components/HelpHeading.jsx';

export default function ApiDocsPage() {
  const container = useRef(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!container.current) return undefined;
    try {
      SwaggerUIBundle({
        url: '/v3/api-docs',
        domNode: container.current,
        deepLinking: false,
        docExpansion: 'list',
        tryItOutEnabled: true,
        requestInterceptor: (request) => {
          const worker = getWorker();
          if (worker && !request.headers['X-Worker']) {
            request.headers['X-Worker'] = encodeURIComponent(worker);
          }
          return request;
        },
        onComplete: () => setError(null),
      });
    } catch (cause) {
      setError(`API 문서를 불러오지 못했습니다: ${cause.message}`);
    }
    const current = container.current;
    return () => {
      current.innerHTML = '';
    };
  }, []);

  return (
    <section className="page">
      <div className="panel">
        <HelpHeading title="API 문서">
          백엔드의 API 문서를 읽어 보여 줍니다.<br />여기서 보낸 요청에는 위쪽 작업자 이름이 붙어 작업 로그에 남습니다.<br />백엔드에서 바로 보려면 <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noreferrer">백엔드 API 문서</a>를 여세요.
        </HelpHeading>
        {error && <div className="notice notice-error">{error}</div>}
        <div className="swagger" ref={container} />
      </div>
    </section>
  );
}
