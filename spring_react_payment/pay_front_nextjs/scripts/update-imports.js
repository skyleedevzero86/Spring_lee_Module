const fs = require('fs');
const path = require('path');

function updateImportsInFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    
    // Replace @/src/ with @/
    content = content.replace(/@\/src\//g, '@/');
    
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`Updated: ${filePath}`);
      return true;
    }
    return false;
  } catch (error) {
    console.error(`Error processing ${filePath}:`, error.message);
    return false;
  }
}

function walkDir(dir, fileList = []) {
  const files = fs.readdirSync(dir);
  
  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);
    
    // Skip node_modules, .next, coverage, and other build directories
    if (file === 'node_modules' || file === '.next' || file === 'coverage' || 
        file === '.git' || file.startsWith('.')) {
      return;
    }
    
    if (stat.isDirectory()) {
      walkDir(filePath, fileList);
    } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
      fileList.push(filePath);
    }
  });
  
  return fileList;
}

const projectRoot = path.resolve(__dirname, '..');
const srcDir = path.join(projectRoot, 'src');
const appDir = path.join(projectRoot, 'app');
const middlewareFile = path.join(projectRoot, 'middleware.ts');

const files = [
  ...walkDir(srcDir),
  ...walkDir(appDir),
  middlewareFile,
].filter(f => f && fs.existsSync(f));

let updatedCount = 0;
files.forEach(file => {
  if (updateImportsInFile(file)) {
    updatedCount++;
  }
});

console.log(`\nTotal files updated: ${updatedCount}`);

