const fs = require('fs');
const path = require('path');

const dirsToClean = [
  path.join(__dirname, '..', '.next'),
  path.join(__dirname, '..', 'node_modules', '.cache'),
  path.join(__dirname, '..', '.swc'),
];

function removeDir(dir, retries = 5) {
  if (!fs.existsSync(dir)) {
    return;
  }
  
  for (let i = 0; i < retries; i++) {
    try {
      if (i > 0) {
        const waitTime = 1000 * (i + 1);
        console.log(`재시도 대기 중... (${waitTime}ms)`);
        const start = Date.now();
        while (Date.now() - start < waitTime) {
        }
      }

      if (process.platform === 'win32') {
        try {
          fs.chmodSync(dir, 0o777);
        } catch {
        }
      }

      fs.rmSync(dir, { 
        recursive: true, 
        force: true, 
        maxRetries: 5, 
        retryDelay: 500 
      });
      console.log(`✓ 삭제됨: ${dir}`);
      return;
    } catch (error) {
      if (i === retries - 1) {
        console.error(`✗ 삭제 실패: ${dir}`, error.message);
        console.error('수동으로 삭제해주세요:', dir);
      }
    }
  }
}

console.log('캐시 정리 시작...');
dirsToClean.forEach((dir) => {
  removeDir(dir);
});
console.log('캐시 정리 완료');

